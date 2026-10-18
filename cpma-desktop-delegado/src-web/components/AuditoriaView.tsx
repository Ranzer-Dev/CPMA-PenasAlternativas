import React, { useState, useEffect } from 'react';
import { ShieldCheck, CheckCircle2, XCircle, Search, Clock, FileText } from 'lucide-react';

interface LogItem {
  id_log: number;
  codigo_tentado?: string;
  data_hora: number;
  mensagem?: string;
  metodo?: string;
  sucesso: number;
  nome_usuario?: string;
  cpf_usuario?: string;
}

interface AuditoriaViewProps {
  onVoltar: () => void;
}

export const AuditoriaView: React.FC<AuditoriaViewProps> = ({ onVoltar }) => {
  const [logs, setLogs] = useState<LogItem[]>([]);
  const [stats, setStats] = useState({
    totalAcessos: 0,
    acessosSucesso: 0,
    acessosFalha: 0,
    taxaSucesso: 100,
    totalApenados: 0,
    totalPenas: 0,
  });
  const [filtro, setFiltro] = useState('');
  const [somenteFalhas, setSomenteFalhas] = useState(false);

  useEffect(() => {
    fetch('/api/v1/auditoria/logs')
      .then(res => res.json())
      .then(setLogs)
      .catch(console.error);

    fetch('/api/v1/auditoria/estatisticas')
      .then(res => res.json())
      .then(setStats)
      .catch(console.error);
  }, []);

  const logsFiltrados = logs.filter(l => {
    if (somenteFalhas && l.sucesso === 1) return false;
    if (!filtro) return true;
    const f = filtro.toLowerCase();
    return (
      (l.nome_usuario && l.nome_usuario.toLowerCase().includes(f)) ||
      (l.codigo_tentado && l.codigo_tentado.includes(f)) ||
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
        <button
          onClick={onVoltar}
          className="px-4 py-2 border border-slate-300 rounded-lg text-xs font-semibold text-slate-700 hover:bg-slate-50 cursor-pointer self-start sm:self-auto"
        >
          Voltar
        </button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs text-center">
          <label className="text-2xs font-bold text-slate-400 uppercase">Total de Acessos</label>
          <p className="text-2xl font-black text-slate-900 mt-1">{stats.totalAcessos}</p>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs text-center">
          <label className="text-2xs font-bold text-emerald-600 uppercase">Sucessos</label>
          <p className="text-2xl font-black text-emerald-600 mt-1">{stats.acessosSucesso}</p>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs text-center">
          <label className="text-2xs font-bold text-red-600 uppercase">Falhas / Recusas</label>
          <p className="text-2xl font-black text-red-600 mt-1">{stats.acessosFalha}</p>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs text-center">
          <label className="text-2xs font-bold text-blue-600 uppercase">Taxa de Sucesso</label>
          <p className="text-2xl font-black text-blue-600 mt-1">{stats.taxaSucesso}%</p>
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
              placeholder="Buscar por apenado, código ou mensagem..."
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
                <th className="py-2.5 px-3">Mensagem do Sistema</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {logsFiltrados.map((log) => {
                const dataStr = new Date(log.data_hora).toLocaleString('pt-BR');
                return (
                  <tr key={log.id_log} className="hover:bg-slate-50">
                    <td className="py-2.5 px-3 font-mono text-slate-600 whitespace-nowrap">
                      {dataStr}
                    </td>
                    <td className="py-2.5 px-3">
                      {log.sucesso === 1 ? (
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
                      {log.nome_usuario || 'Não autenticado'}
                    </td>
                    <td className="py-2.5 px-3 font-mono text-slate-500">
                      {log.metodo || 'PIN'}
                    </td>
                    <td className="py-2.5 px-3 text-slate-600 max-w-sm truncate">
                      {log.mensagem || '-'}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
