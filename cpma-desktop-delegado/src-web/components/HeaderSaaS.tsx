import React, { useEffect, useState } from 'react';
import { BackendStatusResponse } from '../types';
import { api } from '../services/api';
import { Search, Database, WifiOff, ChevronRight } from 'lucide-react';

interface HeaderSaaSProps {
  currentView: string;
  onNavigate: (view: string, idParam?: number) => void;
  termoBusca: string;
  onTermoBuscaChange: (termo: string) => void;
  onBuscarSubmit: (e: React.FormEvent) => void;
  subtitulo?: string;
}

export const HeaderSaaS: React.FC<HeaderSaaSProps> = ({
  currentView,
  onNavigate,
  termoBusca,
  onTermoBuscaChange,
  onBuscarSubmit,
  subtitulo,
}) => {
  const [statusOnline, setStatusOnline] = useState<boolean>(true);
  const [dbLabel, setDbLabel] = useState<string>('Banco Local Ativo');

  useEffect(() => {
    let mounted = true;
    const verificarStatus = async () => {
      try {
        const res = await api.get<BackendStatusResponse>('/totem/status', {
          skipAuthRedirect: true,
          timeoutMs: 3000,
        });
        if (!mounted) return;
        setStatusOnline(res?.status === 'UP');
        setDbLabel(res?.database ? `${res.database} Ativo` : 'API Local Ativa');
      } catch {
        if (!mounted) return;
        setStatusOnline(false);
      }
    };

    verificarStatus();
    const interval = setInterval(verificarStatus, 10000);
    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, []);

  const getTituloView = (view: string) => {
    switch (view) {
      case 'dashboard':
        return 'Painel de Controle';
      case 'usuarios':
        return 'Gestão de Apenados';
      case 'detalhe':
        return 'Prontuário do Apenado';
      case 'instituicoes':
        return 'Instituições Conveniadas';
      case 'penas':
        return 'Penas e Processos Judiciais';
      case 'pontos':
        return 'Frequência e Registro de Ponto';
      case 'facial':
        return 'Identificação Biométrica Facial';
      case 'auditoria':
        return 'Trilha de Auditoria e Segurança';
      default:
        return 'Mesa do Delegado';
    }
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200/90 px-6 flex items-center justify-between sticky top-0 z-20 shadow-2xs print:hidden">
      {/* Left: Breadcrumbs & Page Title */}
      <div className="flex items-center space-x-3">
        <div className="flex items-center space-x-1.5 text-xs text-slate-500 font-medium">
          <span
            className="hover:text-blue-600 cursor-pointer transition-colors"
            onClick={() => onNavigate('dashboard')}
          >
            CPMA
          </span>
          <ChevronRight className="w-3.5 h-3.5 text-slate-400" />
          <span className="text-slate-900 font-bold">{getTituloView(currentView)}</span>
          {subtitulo && (
            <>
              <ChevronRight className="w-3.5 h-3.5 text-slate-400" />
              <span className="text-blue-600 font-semibold truncate max-w-xs">{subtitulo}</span>
            </>
          )}
        </div>
      </div>

      {/* Center: Global Quick Search Input */}
      <div className="hidden md:flex flex-1 max-w-md mx-6">
        <form onSubmit={onBuscarSubmit} className="w-full relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
            <Search className="w-4 h-4" />
          </div>
          <input
            type="text"
            value={termoBusca}
            onChange={(e) => onTermoBuscaChange(e.target.value)}
            placeholder="Buscar apenado por CPF, código ou nome (pressione Enter)..."
            className="w-full pl-9 pr-4 py-1.5 bg-slate-100/80 hover:bg-slate-100 focus:bg-white border border-slate-200 focus:border-blue-500 rounded-xl text-xs text-slate-800 placeholder-slate-400 focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 transition-all font-sans"
          />
        </form>
      </div>

      {/* Right: Status Pill */}
      <div className="flex items-center space-x-3">
        {statusOnline ? (
          <div className="hidden lg:flex items-center space-x-1.5 px-2.5 py-1 rounded-lg bg-emerald-50 text-emerald-700 border border-emerald-200 text-3xs font-semibold">
            <Database className="w-3 h-3 text-emerald-600" />
            <span>{dbLabel}</span>
          </div>
        ) : (
          <div className="hidden lg:flex items-center space-x-1.5 px-2.5 py-1 rounded-lg bg-red-50 text-red-700 border border-red-200 text-3xs font-semibold">
            <WifiOff className="w-3 h-3 text-red-600" />
            <span>API Offline (127.0.0.1:8080)</span>
          </div>
        )}
      </div>
    </header>
  );
};
