import React from 'react';
import { Administrador } from '../types';
import {
  Users,
  Building2,
  Scale,
  Clock,
  Camera,
  LogOut,
  Home,
  Shield,
} from 'lucide-react';

interface NavbarProps {
  admin: Administrador;
  currentView: string;
  onNavigate: (view: string) => void;
  onLogout: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  admin,
  currentView,
  onNavigate,
  onLogout,
}) => {
  const navItems = [
    { id: 'dashboard', label: 'Início', icon: Home },
    { id: 'usuarios', label: 'Apenados', icon: Users },
    { id: 'instituicoes', label: 'Instituições', icon: Building2 },
    { id: 'penas', label: 'Penas', icon: Scale },
    { id: 'pontos', label: 'Registros / Ponto', icon: Clock },
    { id: 'facial', label: 'Biometria Facial', icon: Camera },
  ];

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-40 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Brand & Logos */}
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => onNavigate('dashboard')}>
            <div className="flex items-center space-x-2">
              <img
                src="/images/Votorantim_Brasao.png"
                alt="Brasão Votorantim"
                className="h-10 w-auto object-contain hidden sm:block"
                onError={(e) => { (e.target as HTMLElement).style.display = 'none'; }}
              />
              <div className="h-8 w-px bg-slate-200 hidden sm:block"></div>
              <img
                src="/images/fatec_ra_sorocaba_votorantim_cor.png"
                alt="FATEC Sorocaba"
                className="h-9 w-auto object-contain hidden sm:block"
                onError={(e) => { (e.target as HTMLElement).style.display = 'none'; }}
              />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-extrabold text-2xl text-blue-600 tracking-tight">CPMA</span>
                <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 border border-blue-200">
                  Votorantim
                </span>
              </div>
              <p className="text-xs text-slate-500 font-medium hidden md:block">
                Central de Penas e Medidas Alternativas
              </p>
            </div>
          </div>

          {/* Nav links */}
          <nav className="hidden lg:flex items-center space-x-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = currentView === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => onNavigate(item.id)}
                  className={`flex items-center space-x-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-blue-600 text-white shadow-xs'
                      : 'text-slate-600 hover:text-blue-600 hover:bg-slate-50'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </nav>

          {/* User profile & Logout */}
          <div className="flex items-center space-x-3">
            <div className="hidden sm:flex items-center space-x-2 bg-slate-100 text-slate-700 px-3 py-1.5 rounded-lg text-xs font-medium border border-slate-200">
              <Shield className="w-3.5 h-3.5 text-blue-600" />
              <span className="truncate max-w-[140px]">{admin.nome}</span>
            </div>

            {window.electronAPI?.isElectron && (
              <span className="hidden md:inline-flex items-center px-2 py-1 rounded bg-slate-800 text-white text-3xs font-bold uppercase tracking-wider">
                Desktop
              </span>
            )}

            <button
              onClick={onLogout}
              title="Encerrar Sessão"
              className="flex items-center space-x-1 text-red-600 hover:text-red-700 hover:bg-red-50 px-2.5 py-1.5 rounded-lg text-xs font-semibold border border-red-200 transition-colors cursor-pointer"
            >
              <LogOut className="w-4 h-4" />
              <span className="hidden sm:inline">Sair</span>
            </button>
          </div>
        </div>

        {/* Mobile secondary nav */}
        <div className="lg:hidden flex items-center space-x-1 overflow-x-auto py-2 border-t border-slate-100">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentView === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onNavigate(item.id)}
                className={`flex items-center space-x-1 px-2.5 py-1.5 rounded-md text-xs font-medium whitespace-nowrap ${
                  isActive
                    ? 'bg-blue-600 text-white'
                    : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                <Icon className="w-3.5 h-3.5" />
                <span>{item.label}</span>
              </button>
            );
          })}
        </div>
      </div>
    </header>
  );
};
