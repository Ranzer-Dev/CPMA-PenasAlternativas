import React, { useState, useEffect } from 'react';
import { Usuario } from '../types';
import { formatarCPF } from '../utils/formatters';
import {
  Search,
  Users,
  Building2,
  Scale,
  Clock,
  Camera,
  Smartphone,
  ShieldCheck,
  ChevronRight,
} from 'lucide-react';

interface DashboardViewProps {
  onNavigate: (view: string, idParam?: number) => void;
  onSelecionarApenado: (u: Usuario) => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  onNavigate,
  onSelecionarApenado,
}) => {
  const [termoBusca, setTermoBusca] = useState('');
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [resultados, setResultados] = useState<Usuario[]>([]);
  const [stats, setStats] = useState({
    totalApenados: 0,
    totalPenas: 0,
    totalInstituicoes: 0,
    totalPontos: 0,
  });

  const carregarDados = async () => {
    try {
      const resUsers = await fetch('/api/v1/usuarios');
      if (resUsers.ok) {
        const data = await resUsers.json();
        setUsuarios(data);
        setStats(prev => ({ ...prev, totalApenados: data.length }));
      }
      const resInst = await fetch('/api/v1/instituicoes');
      if (resInst.ok) {
        const insts = await resInst.json();
        setStats(prev => ({ ...prev, totalInstituicoes: insts.length }));
      }
      const resPenas = await fetch('/api/v1/penas');
      if (resPenas.ok) {
        const penas = await resPenas.json();
        setStats(prev => ({ ...prev, totalPenas: penas.length }));
      }
    } catch (e) {
      console.warn('Erro ao carregar dados:', e);
    }
  };

  useEffect(() => {
    carregarDados();
  }, []);

  const handleBuscar = (e: React.FormEvent) => {
    e.preventDefault();
    const termo = termoBusca.trim().toLowerCase();
    const termoNum = termo.replace(/\D/g, '');

    if (!termo) {
      setResultados([]);
      return;
    }

    const filtrados = usuarios.filter(u => {
      const cpfLimpo = u.cpf.replace(/\D/g, '');
      if (termoNum && cpfLimpo.includes(termoNum)) return true;
      if (u.nome.toLowerCase().includes(termo)) return true;
      if (u.codigo && u.codigo.toLowerCase().includes(termo)) return true;
      return false;
    });

    setResultados(filtrados);
    if (filtrados.length === 1) {
      onSelecionarApenado(filtrados[0]);
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-8">
      {/* SaaS Welcome & Quick Stats */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-2 border-b border-slate-200/80">
        <div>
          <h2 className="text-2xl font-black text-slate-900 tracking-tight">Painel de Controle</h2>
          <p className="text-xs text-slate-500 font-medium">
            Central de Penas e Medidas Alternativas • Comarca de Votorantim / SP
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <button
            type="button"
            onClick={() => onNavigate('usuarios', -1)}
            className="px-3.5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold shadow-xs transition-all cursor-pointer flex items-center space-x-1.5"
          >
            <span>+ Cadastrar Apenado</span>
          </button>
          <button
            type="button"
            onClick={() => onNavigate('pontos')}
            className="px-3.5 py-2 bg-white hover:bg-slate-50 text-slate-700 border border-slate-300 rounded-xl text-xs font-bold shadow-2xs transition-all cursor-pointer flex items-center space-x-1.5"
          >
            <span>Registrar Ponto</span>
          </button>
        </div>
      </div>

      {/* KPI Stats Grid - 3 Painéis Solicitados */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-2xs flex items-center justify-between">
          <div>
            <span className="text-3xs font-bold uppercase tracking-wider text-slate-400 block">
              Apenados Cadastrados
            </span>
            <span className="text-3xl font-black text-slate-900 mt-1 block">
              {stats.totalApenados}
            </span>
            <span className="text-xs text-emerald-600 font-semibold flex items-center gap-1.5 mt-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              Ativos no Sistema
            </span>
          </div>
          <div className="w-14 h-14 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center">
            <Users className="w-7 h-7" />
          </div>
        </div>

        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-2xs flex items-center justify-between">
          <div>
            <span className="text-3xs font-bold uppercase tracking-wider text-slate-400 block">
              Processos / Penas
            </span>
            <span className="text-3xl font-black text-slate-900 mt-1 block">
              {stats.totalPenas}
            </span>
            <span className="text-xs text-blue-600 font-semibold flex items-center gap-1.5 mt-1.5">
              <span className="w-2 h-2 rounded-full bg-blue-500 animate-pulse"></span>
              Em Cumprimento
            </span>
          </div>
          <div className="w-14 h-14 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center">
            <Scale className="w-7 h-7" />
          </div>
        </div>

        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-2xs flex items-center justify-between">
          <div>
            <span className="text-3xs font-bold uppercase tracking-wider text-slate-400 block">
              Instituições Parceiras
            </span>
            <span className="text-3xl font-black text-slate-900 mt-1 block">
              {stats.totalInstituicoes}
            </span>
            <span className="text-xs text-emerald-600 font-semibold flex items-center gap-1.5 mt-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
              Conveniadas
            </span>
          </div>
          <div className="w-14 h-14 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
            <Building2 className="w-7 h-7" />
          </div>
        </div>
      </div>

      {/* Busca Exclusiva de Apenados */}
      <div className="bg-white rounded-2xl p-6 sm:p-8 shadow-sm border border-slate-200 max-w-3xl mx-auto space-y-6">
        <div className="text-center space-y-1">
          <h3 className="text-lg font-black text-slate-900">Consulta Rápida de Apenado</h3>
          <p className="text-xs text-slate-500">
            Localize o prontuário pelo nome completo, CPF ou código da pena
          </p>
        </div>

        <form onSubmit={handleBuscar} className="space-y-4">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <Search className="w-5 h-5 text-slate-400 absolute left-3.5 top-3.5" />
              <input
                type="text"
                value={termoBusca}
                onChange={(e) => {
                  setTermoBusca(e.target.value);
                  if (!e.target.value) setResultados([]);
                }}
                placeholder="Digite o nome, CPF ou código para localizar..."
                className="w-full pl-11 pr-4 py-3 bg-white border border-slate-300 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-2xs font-medium"
              />
            </div>
            <button
              type="submit"
              className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl text-sm transition-all shadow-xs flex items-center justify-center space-x-2 cursor-pointer shrink-0"
            >
              <Search className="w-4 h-4" />
              <span>Localizar</span>
            </button>
          </div>
        </form>

        {/* Results dropdown list if multiple */}
        {resultados.length > 0 && (
          <div className="pt-4 border-t border-slate-100 space-y-2">
            <p className="text-xs font-semibold text-slate-500 uppercase">
              Resultados Encontrados ({resultados.length}):
            </p>
            <div className="space-y-1 max-h-64 overflow-y-auto">
              {resultados.map((u) => (
                <div
                  key={u.id_usuario}
                  onClick={() => onSelecionarApenado(u)}
                  className="flex items-center justify-between p-3.5 rounded-xl hover:bg-blue-50 cursor-pointer border border-transparent hover:border-blue-200 transition-colors"
                >
                  <div className="flex items-center space-x-3.5">
                    <div className="w-10 h-10 rounded-xl bg-blue-100/60 text-blue-700 font-bold flex items-center justify-center overflow-hidden shrink-0 border border-blue-200/50">
                      {u.foto ? (
                        <img src={u.foto} alt={u.nome} className="w-full h-full object-cover" />
                      ) : (
                        u.nome.charAt(0)
                      )}
                    </div>
                    <div>
                      <p className="text-sm font-bold text-slate-900">{u.nome}</p>
                      <p className="text-xs text-slate-500 font-mono">
                        CPF: {formatarCPF(u.cpf)} • Cód: {u.codigo || 'S/N'}
                      </p>
                    </div>
                  </div>
                  <ChevronRight className="w-4 h-4 text-blue-600" />
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
