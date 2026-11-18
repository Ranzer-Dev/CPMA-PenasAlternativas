import React, { useState, useEffect } from 'react';
import {
  Server,
  Wifi,
  CheckCircle2,
  AlertTriangle,
  X,
  Save,
  RefreshCw,
  ShieldCheck,
  Monitor,
  Minimize2,
  Maximize2,
  LogOut,
} from 'lucide-react';
import { TotemConfig, TotemStatusResponse } from '../types';
import { getTotemConfig, saveTotemConfig, totemApi } from '../services/totemApi';

interface ConfigServidorModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSaved: (newConfig: TotemConfig, status: TotemStatusResponse | null) => void;
}

export const ConfigServidorModal: React.FC<ConfigServidorModalProps> = ({
  isOpen,
  onClose,
  onSaved,
}) => {
  const [formData, setFormData] = useState<TotemConfig>(getTotemConfig());
  const [testing, setTesting] = useState(false);
  const [testResult, setTestResult] = useState<{
    ok: boolean;
    message: string;
    details?: TotemStatusResponse;
  } | null>(null);

  useEffect(() => {
    if (isOpen) {
      setFormData(getTotemConfig());
      setTestResult(null);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleTestConnection = async () => {
    setTesting(true);
    setTestResult(null);
    try {
      const resp = await totemApi.verificarStatusServidor(formData);
      setTestResult({
        ok: true,
        message: `Conectado com sucesso ao PC do Delegado (${formData.serverIp}:${formData.serverPort})!`,
        details: resp,
      });
    } catch (err: any) {
      setTestResult({
        ok: false,
        message: err?.message || 'Falha ao conectar ao servidor no IP informado.',
      });
    } finally {
      setTesting(false);
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setTesting(true);
    const saved = await saveTotemConfig(formData);
    let statusResp: TotemStatusResponse | null = null;
    try {
      statusResp = await totemApi.verificarStatusServidor(saved);
    } catch {
      statusResp = null;
    } finally {
      setTesting(false);
    }
    onSaved(saved, statusResp);
    onClose();
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      onKeyDown={(e) => e.stopPropagation()}
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/85 backdrop-blur-md p-4 no-print select-text"
    >
      <div className="w-full max-w-xl rounded-2xl border border-slate-700 bg-slate-900 shadow-2xl overflow-hidden text-slate-100">
        {/* Cabeçalho */}
        <div className="flex items-center justify-between border-b border-slate-800 bg-slate-950/60 px-6 py-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600/20 border border-blue-500/40 text-blue-400">
              <Server className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">
                Configuração de Rede do Terminal (Cabo LAN)
              </h2>
              <p className="text-xs text-slate-400">
                Conexão com o Backend no PC do Delegado (Porta 8080)
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-slate-400 hover:bg-slate-800 hover:text-white transition"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <form onSubmit={handleSave} className="p-6 space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                IP do PC do Delegado (Rede Local / Cabo)
              </label>
              <input
                type="text"
                required
                autoFocus
                placeholder="Ex: 192.168.0.10 ou 127.0.0.1"
                value={formData.serverIp}
                onChange={(e) => setFormData({ ...formData, serverIp: e.target.value })}
                className="w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-base font-mono text-white placeholder-slate-500 focus:border-blue-500 focus:outline-none select-text"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                Porta da API
              </label>
              <input
                type="number"
                required
                min={1}
                max={65535}
                value={formData.serverPort || ''}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    serverPort: e.target.value === '' ? 0 : Number(e.target.value),
                  })
                }
                onBlur={() => {
                  if (!formData.serverPort) {
                    setFormData((prev) => ({ ...prev, serverPort: 8080 }));
                  }
                }}
                className="w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-base font-mono text-white focus:border-blue-500 focus:outline-none select-text"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                Identificador do Totem
              </label>
              <input
                type="text"
                value={formData.terminalId}
                onChange={(e) => setFormData({ ...formData, terminalId: e.target.value })}
                className="w-full rounded-xl border border-slate-700 bg-slate-950 px-3.5 py-2.5 text-sm font-mono text-slate-200 focus:border-blue-500 focus:outline-none select-text"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                Chave de Segurança (X-Totem-Terminal-Key)
              </label>
              <input
                type="password"
                value={formData.terminalKey}
                onChange={(e) => setFormData({ ...formData, terminalKey: e.target.value })}
                className="w-full rounded-xl border border-slate-700 bg-slate-950 px-3.5 py-2.5 text-sm font-mono text-slate-200 focus:border-blue-500 focus:outline-none select-text"
              />
            </div>
          </div>

          <div className="rounded-xl border border-slate-800 bg-slate-950/70 p-3.5 text-xs text-slate-400 flex items-start gap-2.5">
            <ShieldCheck className="h-4 w-4 text-emerald-400 shrink-0 mt-0.5" />
            <div>
              <span className="font-semibold text-slate-200">Proteção do Banco de Dados:</span> O
              Totem comunica exclusivamente via HTTP REST na porta{' '}
              <code className="text-blue-400">{formData.serverPort}</code>. O banco de dados no PC
              do Delegado permanece isolado em <code className="text-emerald-400">127.0.0.1</code>{' '}
              sem porta exposta na rede.
            </div>
          </div>

          {testResult && (
            <div
              className={`rounded-xl border p-4 text-sm flex items-start gap-3 ${
                testResult.ok
                  ? 'border-emerald-500/40 bg-emerald-950/40 text-emerald-200'
                  : 'border-rose-500/40 bg-rose-950/40 text-rose-200'
              }`}
            >
              {testResult.ok ? (
                <CheckCircle2 className="h-5 w-5 text-emerald-400 shrink-0 mt-0.5" />
              ) : (
                <AlertTriangle className="h-5 w-5 text-rose-400 shrink-0 mt-0.5" />
              )}
              <div className="space-y-1">
                <p className="font-semibold">{testResult.message}</p>
                {testResult.details && (
                  <p className="text-xs opacity-90">
                    Motor Facial no Servidor:{' '}
                    <span className="font-bold">{testResult.details.facialService}</span> | Banco:{' '}
                    <span className="font-bold">{testResult.details.database || 'Ativo'}</span>
                  </p>
                )}
              </div>
            </div>
          )}

          {/* Controles da Janela Electron (Operador) */}
          {window.electronAPI && (
            <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-slate-800">
              <span className="text-xs text-slate-400 flex items-center gap-1.5">
                <Monitor className="h-3.5 w-3.5" /> Controles do Modo Kiosk:
              </span>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => window.electronAPI?.exitKiosk()}
                  className="inline-flex items-center gap-1.5 rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-xs font-medium text-slate-200 hover:bg-slate-700"
                >
                  <Minimize2 className="h-3.5 w-3.5" /> Sair Tela Cheia
                </button>
                <button
                  type="button"
                  onClick={() => window.electronAPI?.enterKiosk()}
                  className="inline-flex items-center gap-1.5 rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-xs font-medium text-slate-200 hover:bg-slate-700"
                >
                  <Maximize2 className="h-3.5 w-3.5" /> Modo Kiosk
                </button>
                <button
                  type="button"
                  onClick={() => window.electronAPI?.quitApp()}
                  className="inline-flex items-center gap-1.5 rounded-lg border border-rose-800 bg-rose-950/60 px-3 py-1.5 text-xs font-medium text-rose-300 hover:bg-rose-900/70"
                >
                  <LogOut className="h-3.5 w-3.5" /> Fechar Totem
                </button>
              </div>
            </div>
          )}

          {/* Rodapé */}
          <div className="flex items-center justify-between gap-3 pt-3 border-t border-slate-800">
            <button
              type="button"
              disabled={testing}
              onClick={handleTestConnection}
              className="inline-flex items-center gap-2 rounded-xl border border-blue-500/40 bg-blue-950/40 px-4 py-2.5 text-sm font-semibold text-blue-300 hover:bg-blue-900/50 disabled:opacity-50 transition"
            >
              {testing ? (
                <RefreshCw className="h-4 w-4 animate-spin" />
              ) : (
                <Wifi className="h-4 w-4" />
              )}
              Testar Conexão LAN
            </button>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={onClose}
                className="rounded-xl border border-slate-700 bg-slate-800 px-4 py-2.5 text-sm font-medium text-slate-300 hover:bg-slate-700 transition"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={testing}
                className="inline-flex items-center gap-2 rounded-xl bg-emerald-600 px-5 py-2.5 text-sm font-bold text-white shadow-lg shadow-emerald-900/30 hover:bg-emerald-500 disabled:opacity-50 transition"
              >
                <Save className="h-4 w-4" />
                Salvar e Conectar
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};
