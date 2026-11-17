import { Administrador } from '../types';

const STORAGE_ADMIN_KEY = 'cpma_admin_session';
const STORAGE_TOKEN_KEY = 'cpma_jwt_token';
const STORAGE_API_URL_KEY = 'cpma_api_url';
const DEFAULT_LOCAL_API_URL = 'http://127.0.0.1:8080/api/v1';
const TOTEM_TERMINAL_KEY = 'CPMA-TOTEM-KIOSK-TERMINAL-01-SP-GOV-AUTH-2026';

declare global {
  interface Window {
    electronAPI?: {
      isElectron?: boolean;
      platform?: string;
      getVersion?: () => Promise<string>;
      getApiBaseUrl?: () => Promise<string>;
      setApiBaseUrl?: (url: string) => Promise<string>;
      minimize?: () => void;
      maximize?: () => void;
      close?: () => void;
      toggleFullscreen?: () => void;
      print?: () => void;
    };
  }
}

export interface FieldValidationError {
  field: string;
  message: string;
}

export class ApiError extends Error {
  status: number;
  code: string;
  fieldErrors: Record<string, string>;

  constructor(
    message: string,
    status = 500,
    code = 'API_ERROR',
    fieldErrors: Record<string, string> = {}
  ) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors;
  }
}

let cachedBaseUrl: string | null = null;
let unauthorizedHandler: (() => void) | null = null;

export function setOnUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler;
}

export function getApiBaseUrlSync(): string {
  if (cachedBaseUrl) {
    return cachedBaseUrl;
  }
  const saved = localStorage.getItem(STORAGE_API_URL_KEY);
  if (saved && saved.trim()) {
    cachedBaseUrl = saved.trim().replace(/\/+$/, '');
    return cachedBaseUrl;
  }
  return DEFAULT_LOCAL_API_URL;
}

export async function initApiBaseUrl(): Promise<string> {
  try {
    if (window.electronAPI?.getApiBaseUrl) {
      const electronUrl = await window.electronAPI.getApiBaseUrl();
      if (electronUrl && electronUrl.trim()) {
        cachedBaseUrl = electronUrl.trim().replace(/\/+$/, '');
        return cachedBaseUrl;
      }
    }
  } catch {
    // fallback para URL padrão local
  }
  return getApiBaseUrlSync();
}

export function salvarSessaoAdmin(admin: Administrador): void {
  localStorage.setItem(STORAGE_ADMIN_KEY, JSON.stringify(admin));
  if (admin.token) {
    localStorage.setItem(STORAGE_TOKEN_KEY, admin.token);
  }
}

export function carregarSessaoAdmin(): Administrador | null {
  try {
    const raw = localStorage.getItem(STORAGE_ADMIN_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as Administrador;
    if (!parsed || !parsed.token || !parsed.adminId) {
      limparSessaoAdmin();
      return null;
    }
    return parsed;
  } catch {
    limparSessaoAdmin();
    return null;
  }
}

export function limparSessaoAdmin(): void {
  localStorage.removeItem(STORAGE_ADMIN_KEY);
  localStorage.removeItem(STORAGE_TOKEN_KEY);
}

export function obterTokenJwt(): string | null {
  const token = localStorage.getItem(STORAGE_TOKEN_KEY);
  if (token) return token;
  const admin = carregarSessaoAdmin();
  return admin?.token ?? null;
}

export function normalizarFotoBase64(foto?: string | null): string | undefined {
  if (!foto || !foto.trim()) return undefined;
  const trimmed = foto.trim();
  if (trimmed.startsWith('data:image/')) {
    return trimmed;
  }
  return `data:image/jpeg;base64,${trimmed}`;
}

export function extrairBase64Puro(foto?: string | null): string | undefined {
  if (!foto || !foto.trim()) return undefined;
  const trimmed = foto.trim();
  const commaIdx = trimmed.indexOf(',');
  if (trimmed.startsWith('data:image/') && commaIdx !== -1) {
    return trimmed.substring(commaIdx + 1);
  }
  return trimmed;
}

export interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown;
  skipAuthRedirect?: boolean;
  timeoutMs?: number;
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const baseUrl = getApiBaseUrlSync();
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  const url = `${baseUrl}${cleanPath}`;

  const headers: Record<string, string> = {
    Accept: 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  };

  if (options.body !== undefined && !(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  const token = obterTokenJwt();
  if (token && !headers['Authorization']) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  if (cleanPath.startsWith('/totem') && !headers['X-Totem-Terminal-Key']) {
    headers['X-Totem-Terminal-Key'] = TOTEM_TERMINAL_KEY;
  }

  const controller = new AbortController();
  const timeout = options.timeoutMs ?? 15000;
  const timer = setTimeout(() => controller.abort(), timeout);

  try {
    const response = await fetch(url, {
      ...options,
      headers,
      body:
        options.body === undefined
          ? undefined
          : options.body instanceof FormData
          ? options.body
          : JSON.stringify(options.body),
      signal: controller.signal,
    });

    clearTimeout(timer);

    if (response.status === 204) {
      return undefined as T;
    }

    const contentType = response.headers.get('content-type') || '';
    let data: any = null;
    if (contentType.includes('application/json')) {
      data = await response.json().catch(() => null);
    } else {
      const text = await response.text().catch(() => '');
      try {
        data = text ? JSON.parse(text) : null;
      } catch {
        data = { message: text };
      }
    }

    if (!response.ok) {
      const fieldErrors: Record<string, string> = {};
      if (Array.isArray(data?.errors)) {
        for (const item of data.errors as FieldValidationError[]) {
          if (item?.field && item?.message) {
            fieldErrors[item.field] = item.message;
          }
        }
      }

      const rawMessage =
        data?.message ||
        data?.mensagem ||
        data?.error ||
        `Erro HTTP ${response.status} ao comunicar com o servidor.`;
      const code = data?.code || (response.status === 401 ? 'UNAUTHORIZED' : 'API_ERROR');

      if (
        response.status === 401 &&
        !options.skipAuthRedirect &&
        !cleanPath.startsWith('/auth/')
      ) {
        limparSessaoAdmin();
        if (unauthorizedHandler) {
          unauthorizedHandler();
        }
      }

      throw new ApiError(rawMessage, response.status, code, fieldErrors);
    }

    return data as T;
  } catch (err: any) {
    clearTimeout(timer);
    if (err instanceof ApiError) {
      throw err;
    }
    if (err?.name === 'AbortError') {
      throw new ApiError(
        'Tempo limite esgotado ao conectar ao servidor backend (porta 8080).',
        0,
        'TIMEOUT_ERROR'
      );
    }
    throw new ApiError(
      'Servidor backend (127.0.0.1:8080) indisponível. Verifique se a API Spring Boot está em execução.',
      0,
      'NETWORK_ERROR'
    );
  }
}

export const api = {
  get: <T>(path: string, options?: RequestOptions) =>
    apiRequest<T>(path, { ...options, method: 'GET' }),
  post: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    apiRequest<T>(path, { ...options, method: 'POST', body }),
  put: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    apiRequest<T>(path, { ...options, method: 'PUT', body }),
  delete: <T>(path: string, options?: RequestOptions) =>
    apiRequest<T>(path, { ...options, method: 'DELETE' }),
};
