import React, { useState, useEffect } from 'react';
import { LogAuditoriaDTO, EstatisticasAuditoriaDTO } from '../types';
import { api } from '../services/api';
import { ShieldCheck, CheckCircle2, XCircle, Search, RefreshCw, AlertCircle } from 'lucide-react';

interface AuditoriaViewProps {
  onVoltar: () => void;
}

export const AuditoriaView: React.FC<AuditoriaViewProps> = ({ onVoltar }) => {
  const [logs, setLogs] = useState<LogAuditoriaDTO[]>([]);
  const [stats, setStats] = useState<EstatisticasAuditoriaDTO>({
    totalLogs: 0,
    totalSucessos: 0,
    totalFalhas: 0,
    falhasBiometricas: 0,
    falhasCodigo: 0,
  });
  const [filtro, setFiltro] = useState('');
  const [somenteFalhas, setSomenteFalhas] = useState(false);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const carregarAuditoria = async () => {
    setCarregando(true);
    setErro(null);
    try {
      const [logsData, statsData] = await Promise.all([
        api.get<LogAuditoriaDTO[]>('/auditoria/logs'),
        api.get<EstatisticasAuditoriaDTO>('/auditoria/estatisticas'),
      ]);
      setLogs(logsData || []);
      if (statsData) {
        setStats(statsData);
      }
    } catch (e: any) {
      setErro(e?.message || 'Erro ao consultar trilha de auditoria do servidor.');
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    carregarAuditoria();
  }, []);

  const taxaSucesso =
    stats.totalLogs > 0 ? Math.round((stats.totalSucessos / stats.totalLogs) * 100) : 100;

  const logsFiltrados = logs.filter((l) => {
    if (somenteFalhas && l.sucesso) return false;
    if (!filtro) return true;
    const f = filtro.toLowerCase();
    return (
      (l.usuarioNome && l.usuarioNome.toLowerCase().includes(f)) ||
      (l.usuarioCpf && l.usuarioCpf.toLowerCase().includes(f)) ||
      (l.codigoTentado && l.codigoTentado.toLowerCase().includes(f)) ||
      (l.metodo && l.metodo.toLowerCase().includes(f)) ||
      (l.mensagem && l.mensagem.toLowerCase().includes(f))
    );
  });

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 flex items-center space-x-2">
            <ShieldCheck className="w-6 h-6 text-emerald-600" />
            <span>Auditoria de Acessos e Totem Kiosk</span>
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Logs oficiais de validação biométrica, tentativas no terminal e conformidade LGPD
          </p>
        </div>
        <div className="flex items-center space-x-2 self-start sm:self-auto">
          <button
            onClick={carregarAuditoria}
            disabled={carregando}
            className="px-3.5 py-2 border border-slate-300 bg-white rounded-lg text-xs font-semibold text-slate-700 hover:bg-slate-50 cursor-pointer flex items-center space-x-1.5"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${carregando ? 'animate-spin' : ''}`} />
            <span>Atualizar</span>
          </button>
          <button
            onClick={onVoltar}
            className="px-4 py-2 border border-slate-300 rounded-lg text-xs font-semibold text-slate-700 hover:bg-slate-50 cursor-pointer"
          >
            Voltar
          </button>
        </div>
      </div>

      {erro && (
        <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center space-x-2">
          <AlertCircle className="w-4 h-4 shrink-0 text-red-600" />
          <span>{erro}</span>
        </div>
      )}

      {/* Stats Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs text-center">
          <label className="text-2xs font-bold text-slate-400 uppercase">Total de Acessos</label>
          <p className="text-2xl font-black text-slate-900 mt-1">{stats.totalLogs}</p>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs text-center">
          <label className="text-2xs font-bold text-emerald-600 uppercase">Sucessos</label>
          <p className="text-2xl font-black text-emerald-600 mt-1">{stats.totalSucessos}</p>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs text-center">
          <label className="text-2xs font-bold text-red-600 uppercase">Falhas / Recusas</label>
          <p className="text-2xl font-black text-red-600 mt-1">{stats.totalFalhas}</p>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs text-center">
          <label className="text-2xs font-bold text-blue-600 uppercase">Taxa de Sucesso</label>
          <p className="text-2xl font-black text-blue-600 mt-1">{taxaSucesso}%</p>
        </div>
      </div>

      {/* Logs Table Card */}
      <div className="bg-white rounded-2xl p-6 sm:p-8 border border-slate-200 shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="relative flex-1 max-w-sm">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={filtro}
              onChange={(e) => setFiltro(e.target.value)}
              placeholder="Buscar por apenado, código, método ou mensagem..."
              className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-hidden"
            />
          </div>

          <label className="flex items-center space-x-2 text-xs font-semibold text-slate-700 cursor-pointer">
            <input
              type="checkbox"
              checked={somenteFalhas}
              onChange={(e) => setSomenteFalhas(e.target.checked)}
              className="rounded text-emerald-600 focus:ring-emerald-500"
            />
            <span>Exibir apenas falhas</span>
          </label>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-50 text-slate-700 uppercase font-bold border-b border-slate-200">
                <th className="py-2.5 px-3">Data e Hora</th>
                <th className="py-2.5 px-3">Status</th>
                <th className="py-2.5 px-3">Apenado</th>
                <th className="py-2.5 px-3">Método</th>
                <th className="py-2.5 px-3">Risco</th>
                <th className="py-2.5 px-3">Mensagem do Sistema</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {logsFiltrados.map((log) => {
                const dataStr = log.dataHora
                  ? new Date(log.dataHora).toLocaleString('pt-BR')
                  : '-';
                return (
                  <tr key={log.idLog} className="hover:bg-slate-50">
                    <td className="py-2.5 px-3 font-mono text-slate-600 whitespace-nowrap">
                      {dataStr}
                    </td>
                    <td className="py-2.5 px-3">
                      {log.sucesso ? (
                        <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 font-bold">
                          <CheckCircle2 className="w-3 h-3" />
                          <span>SUCESSO</span>
                        </span>
                      ) : (
                        <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full bg-red-50 text-red-700 font-bold">
                          <XCircle className="w-3 h-3" />
                          <span>FALHA</span>
                        </span>
                      )}
                    </td>
                    <td className="py-2.5 px-3 font-semibold text-slate-800">
                      {log.usuarioNome || 'Não autenticado'}
                      {log.usuarioCpf ? ` (${log.usuarioCpf})` : ''}
                    </td>
                    <td className="py-2.5 px-3 font-mono text-slate-500">
                      {log.metodo || 'PIN'}
                    </td>
                    <td className="py-2.5 px-3">
                      <span
                        className={`px-2 py-0.5 rounded font-bold text-3xs uppercase ${
                          log.nivelRisco === 'CRITICO'
                            ? 'bg-red-100 text-red-800'
                            : log.nivelRisco === 'ALTO'
                            ? 'bg-amber-100 text-amber-800'
                            : 'bg-slate-100 text-slate-600'
                        }`}
                      >
                        {log.nivelRisco || 'NORMAL'}
                      </span>
                    </td>
                    <td className="py-2.5 px-3 text-slate-600 max-w-sm truncate" title={log.mensagem}>
                      {log.mensagem || '-'}
                    </td>
                  </tr>
                );
              })}
              {logsFiltrados.length === 0 && (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-slate-400">
                    Nenhum registro de auditoria encontrado.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
