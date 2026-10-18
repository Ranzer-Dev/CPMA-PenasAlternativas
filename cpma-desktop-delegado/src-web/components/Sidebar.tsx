import React from 'react';
import { Administrador } from '../types';
import {
  LayoutDashboard,
  Users,
  Building2,
  Scale,
  Clock,
  Camera,
  ShieldCheck,
  LogOut,
  ChevronLeft,
  ChevronRight,
  UserCheck,
} from 'lucide-react';

interface SidebarProps {
  admin: Administrador | null;
  currentView: string;
  onNavigate: (view: string) => void;
  onLogout: () => void;
  collapsed: boolean;
  onToggleCollapse: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  admin,
  currentView,
  onNavigate,
  onLogout,
  collapsed,
  onToggleCollapse,
}) => {
  const navSections = [
    {
      title: 'VISÃO GERAL',
      items: [
        { id: 'dashboard', label: 'Painel Geral', icon: LayoutDashboard },
      ],
    },
    {
      title: 'EXECUÇÃO PENAL',
      items: [
        { id: 'usuarios', label: 'Apenados', icon: Users },
        { id: 'penas', label: 'Penas Alternativas', icon: Scale },
        { id: 'instituicoes', label: 'Instituições', icon: Building2 },
        { id: 'pontos', label: 'Frequência / Ponto', icon: Clock },
      ],
    },
    {
      title: 'AUDITORIA & CONFORMIDADE',
      items: [
        { id: 'auditoria', label: 'Trilha de Auditoria', icon: ShieldCheck },
      ],
    },
  ];

  return (
    <aside
      className={`bg-slate-900 border-r border-slate-800 text-slate-300 flex flex-col justify-between transition-all duration-300 z-30 select-none ${
        collapsed ? 'w-20' : 'w-64'
      }`}
    >
      {/* Top Branding Section */}
      <div>
        <div className="h-16 flex items-center px-4 border-b border-slate-800/80 justify-between">
          <div
            className="flex items-center space-x-3 cursor-pointer overflow-hidden"
            onClick={() => onNavigate('dashboard')}
          >
            <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shrink-0 p-1">
              <img
                src="/images/logoSP.png"
                alt="Brasão SP"
                className="w-full h-full object-contain"
                onError={(e) => {
                  (e.target as HTMLElement).style.display = 'none';
                }}
              />
            </div>
            {!collapsed && (
              <div className="flex flex-col truncate">
                <span className="font-black text-lg text-white tracking-tight flex items-center gap-1.5">
                  CPMA
                  <span className="text-3xs uppercase bg-blue-600/30 text-blue-400 border border-blue-500/30 font-bold px-1.5 py-0.5 rounded">
                    SaaS
                  </span>
                </span>
                <span className="text-3xs text-slate-400 font-medium truncate">
                  Mesa do Delegado
                </span>
              </div>
            )}
          </div>

          {/* Collapse button */}
          <button
            type="button"
            onClick={onToggleCollapse}
            title={collapsed ? 'Expandir Menu' : 'Recolher Menu'}
            className="w-7 h-7 rounded-lg bg-slate-800/80 hover:bg-slate-700 text-slate-400 hover:text-white flex items-center justify-center transition-colors cursor-pointer shrink-0"
          >
            {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
          </button>
        </div>

        {/* Navigation Sections */}
        <div className="p-3 space-y-6 overflow-y-auto max-h-[calc(100vh-160px)]">
          {navSections.map((section, idx) => (
            <div key={idx} className="space-y-1">
              {!collapsed && (
                <div className="px-3 text-3xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                  {section.title}
                </div>
              )}
              {section.items.map((item) => {
                const Icon = item.icon;
                const isActive = currentView === item.id;
                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => onNavigate(item.id)}
                    title={collapsed ? item.label : undefined}
                    className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all cursor-pointer ${
                      isActive
                        ? 'bg-blue-600 text-white shadow-md shadow-blue-600/20'
                        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                    } ${collapsed ? 'justify-center px-0' : ''}`}
                  >
                    <Icon className={`w-4 h-4 shrink-0 ${isActive ? 'text-white' : 'text-slate-400'}`} />
                    {!collapsed && <span className="truncate">{item.label}</span>}
                  </button>
                );
              })}
            </div>
          ))}
        </div>
      </div>

      {/* Footer Profile & Logout */}
      <div className="p-3 border-t border-slate-800/80 bg-slate-950/40">
        <div className={`flex items-center ${collapsed ? 'justify-center' : 'justify-between'}`}>
          <div className="flex items-center space-x-2.5 overflow-hidden">
            <div className="w-8 h-8 rounded-lg bg-linear-to-tr from-blue-600 to-indigo-600 text-white flex items-center justify-center font-bold text-xs shrink-0 shadow-xs">
              {admin?.nome ? admin.nome.charAt(0) : 'D'}
            </div>
            {!collapsed && (
              <div className="flex flex-col truncate">
                <span className="text-xs font-bold text-white truncate">
                  {admin?.nome || 'Delegado Titular'}
                </span>
                <span className="text-3xs text-emerald-400 flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                  Sessão Ativa
                </span>
              </div>
            )}
          </div>

          {!collapsed && (
            <button
              type="button"
              onClick={onLogout}
              title="Encerrar Sessão"
              className="p-1.5 rounded-lg text-slate-400 hover:text-red-400 hover:bg-red-950/30 transition-colors cursor-pointer"
            >
              <LogOut className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </aside>
  );
};
