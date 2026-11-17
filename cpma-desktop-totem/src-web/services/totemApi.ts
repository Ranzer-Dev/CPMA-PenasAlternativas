import {
  TotemConfig,
  TotemStatusResponse,
  ValidarCodigoEtapa1Response,
  ValidarAcessoResponse,
  UsuarioResponse,
  PenaResponse,
  RegistroTrabalhoResponse,
  ResumoCumprimentoResponse,
  ProntuarioApenado,
} from '../types';

const STORAGE_KEY = 'cpma_totem_config_v1';

const DEFAULT_TOTEM_CONFIG: TotemConfig = {
  serverIp: '127.0.0.1',
  serverPort: 8080,
  terminalId: 'TOTEM-DELEGACIA-01',
  terminalKey: 'CPMA-TOTEM-TERMINAL-SECRET-2026-KEY',
};

let currentConfig: TotemConfig = loadFromLocalStorage();

function loadFromLocalStorage(): TotemConfig {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      return {
        ...DEFAULT_TOTEM_CONFIG,
        ...parsed,
        serverPort: Number(parsed.serverPort || 8080),
      };
    }
  } catch {
    // fallback silencioso
  }
  return { ...DEFAULT_TOTEM_CONFIG };
}

export async function initTotemConfig(): Promise<TotemConfig> {
  if (window.electronAPI?.getConfig) {
    try {
      const electronCfg = await window.electronAPI.getConfig();
      if (electronCfg && electronCfg.serverIp) {
        currentConfig = {
          ...currentConfig,
          ...electronCfg,
          serverPort: Number(electronCfg.serverPort || 8080),
        };
        localStorage.setItem(STORAGE_KEY, JSON.stringify(currentConfig));
      }
    } catch {
      // mantém config local
    }
  }
  return currentConfig;
}

export function getTotemConfig(): TotemConfig {
  return { ...currentConfig };
}

export async function saveTotemConfig(partial: Partial<TotemConfig>): Promise<TotemConfig> {
  currentConfig = {
    ...currentConfig,
    ...partial,
    serverIp: String(partial.serverIp ?? currentConfig.serverIp).trim() || '127.0.0.1',
    serverPort: Number(partial.serverPort ?? currentConfig.serverPort) || 8080,
    terminalId: String(partial.terminalId ?? currentConfig.terminalId).trim() || 'TOTEM-DELEGACIA-01',
    terminalKey:
      String(partial.terminalKey ?? currentConfig.terminalKey).trim() ||
      'CPMA-TOTEM-TERMINAL-SECRET-2026-KEY',
  };

  localStorage.setItem(STORAGE_KEY, JSON.stringify(currentConfig));

  if (window.electronAPI?.saveConfig) {
    try {
      await window.electronAPI.saveConfig(currentConfig);
    } catch {
      // ignora erro IPC
    }
  }

  return { ...currentConfig };
}

export function getTotemApiBaseUrl(overrideConfig?: Partial<TotemConfig>): string {
  const ip = (overrideConfig?.serverIp ?? currentConfig.serverIp).trim();
  const port = overrideConfig?.serverPort ?? currentConfig.serverPort ?? 8080;
  return `http://${ip}:${port}/api/v1`;
}

export function normalizarFotoBase64(foto?: string | null): string | undefined {
  if (!foto) return undefined;
  if (foto.startsWith('data:image/')) return foto;
  return `data:image/jpeg;base64,${foto}`;
}

export class TotemApiError extends Error {
  status: number;
  errorCode?: string;

  constructor(message: string, status: number, errorCode?: string) {
    super(message);
    this.name = 'TotemApiError';
    this.status = status;
    this.errorCode = errorCode;
  }
}

async function totemFetch<T>(
  endpoint: string,
  options: RequestInit = {},
  optionsExtra: { timeoutMs?: number; overrideConfig?: Partial<TotemConfig>; bearerToken?: string } = {},
): Promise<T> {
  const baseUrl = getTotemApiBaseUrl(optionsExtra.overrideConfig);
  const terminalKey = optionsExtra.overrideConfig?.terminalKey ?? currentConfig.terminalKey;
  const timeoutMs = optionsExtra.timeoutMs ?? 12000;

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'X-Totem-Terminal-Key': terminalKey,
    ...(options.headers as Record<string, string> | undefined),
  };

  if (optionsExtra.bearerToken) {
    headers['Authorization'] = `Bearer ${optionsExtra.bearerToken}`;
  }

  try {
    const response = await fetch(`${baseUrl}${endpoint}`, {
      ...options,
      headers,
      signal: controller.signal,
    });

    if (response.status === 204) {
      return undefined as unknown as T;
    }

    const contentType = response.headers.get('content-type') || '';
    const isJson = contentType.includes('application/json');
    const body = isJson ? await response.json().catch(() => null) : await response.text().catch(() => '');

    if (!response.ok) {
      const msg =
        (body && typeof body === 'object' && (body.message || body.motivo || body.mensagem || body.error)) ||
        `Erro HTTP ${response.status} ao comunicar com o servidor da Delegacia.`;
      const code = body && typeof body === 'object' ? body.errorCode : undefined;
      throw new TotemApiError(msg, response.status, code);
    }

    return body as T;
  } catch (err: any) {
    if (err instanceof TotemApiError) {
      throw err;
    }
    if (err?.name === 'AbortError') {
      throw new TotemApiError(
        `Tempo limite esgotado ao conectar ao PC do Delegado (${baseUrl}). Verifique o cabo de rede.`,
        0,
        'NETWORK_TIMEOUT',
      );
    }
    throw new TotemApiError(
      `Sem conexão com o PC do Delegado (${baseUrl}). Verifique o cabo de rede ou o IP configurado.`,
      0,
      'NETWORK_OFFLINE',
    );
  } finally {
    clearTimeout(timer);
  }
}

export const totemApi = {
  /**
   * Verifica conectividade via cabo de rede com o cpma-backend no PC do Delegado
   */
  verificarStatusServidor: (overrideConfig?: Partial<TotemConfig>) =>
    totemFetch<TotemStatusResponse>(
      '/totem/status',
      { method: 'GET' },
      { timeoutMs: 3500, overrideConfig },
    ),

  /**
   * Etapa 1: Valida o PIN de 6 dígitos sem consumi-lo
   */
  validarCodigoEtapa1: (codigo: string) =>
    totemFetch<ValidarCodigoEtapa1Response>('/totem/etapa1/validar-codigo', {
      method: 'POST',
      body: JSON.stringify({
        codigo: codigo.trim(),
        ipOrigem: `LAN:${currentConfig.terminalId}`,
      }),
    }),

  /**
   * Etapa 2: Valida a biometria facial contra a foto oficial no backend e consome o PIN,
   * retornando o token JWT de escopo APENADO_SESSION (válido por 10 minutos).
   */
  validarBiometriaEtapa2: (codigo: string, fotoCapturadaBase64: string, threshold = 0.6) =>
    totemFetch<ValidarAcessoResponse>(
      '/totem/etapa2/validar-biometria',
      {
        method: 'POST',
        body: JSON.stringify({
          codigo: codigo.trim(),
          fotoCapturadaBase64,
          threshold,
          ipOrigem: `LAN:${currentConfig.terminalId}`,
        }),
      },
      { timeoutMs: 20000 },
    ),

  /**
   * Etapa 3: Carrega os dados completos do prontuário do apenado autenticado com o token APENADO_SESSION
   */
  carregarProntuarioApenado: async (
    usuarioId: number,
    tokenSessao: string,
  ): Promise<ProntuarioApenado> => {
    const [usuario, penas] = await Promise.all([
      totemFetch<UsuarioResponse>(
        `/usuarios/${usuarioId}`,
        { method: 'GET' },
        { bearerToken: tokenSessao },
      ),
      totemFetch<PenaResponse[]>(
        `/penas/usuario/${usuarioId}`,
        { method: 'GET' },
        { bearerToken: tokenSessao },
      ),
    ]);

    const penasOrdenadas = [...(penas || [])].sort((a, b) => b.id - a.id);
    const penaPrincipal =
      penasOrdenadas.find((p) => (p.horasCumpridas || 0) < (p.horasCumprir || 0)) ||
      penasOrdenadas[0] ||
      null;

    let resumo: ResumoCumprimentoResponse | null = null;
    let registros: RegistroTrabalhoResponse[] = [];

    if (penaPrincipal) {
      const [resumoResp, registrosResp] = await Promise.all([
        totemFetch<ResumoCumprimentoResponse>(
          `/registros-trabalho/pena/${penaPrincipal.id}/resumo`,
          { method: 'GET' },
          { bearerToken: tokenSessao },
        ).catch(() => null),
        totemFetch<RegistroTrabalhoResponse[]>(
          `/registros-trabalho/pena/${penaPrincipal.id}`,
          { method: 'GET' },
          { bearerToken: tokenSessao },
        ).catch(() => []),
      ]);
      resumo = resumoResp;
      registros = (registrosResp || []).sort((a, b) =>
        (b.dataTrabalho || '').localeCompare(a.dataTrabalho || ''),
      );
    }

    return {
      usuario: {
        ...usuario,
        foto: normalizarFotoBase64(usuario.foto),
      },
      penas: penasOrdenadas,
      penaPrincipal,
      resumo,
      registros,
    };
  },
};
