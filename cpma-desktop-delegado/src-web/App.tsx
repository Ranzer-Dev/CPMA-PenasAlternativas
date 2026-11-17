import React, { useState, useEffect } from 'react';
import { Administrador, Usuario } from './types';
import {
  api,
  carregarSessaoAdmin,
  initApiBaseUrl,
  limparSessaoAdmin,
  setOnUnauthorizedHandler,
} from './services/api';
import { Sidebar } from './components/Sidebar';
import { HeaderSaaS } from './components/HeaderSaaS';
import { LoginView } from './components/LoginView';
import { RedefinirSenhaView } from './components/RedefinirSenhaView';
import { DashboardView } from './components/DashboardView';
import { DetalheApenadoView } from './components/DetalheApenadoView';
import { CadastroUsuarioView } from './components/CadastroUsuarioView';
import { CadastroInstituicaoView } from './components/CadastroInstituicaoView';
import { CadastroPenaView } from './components/CadastroPenaView';
import { CadastroRegistroDeTrabalhoView } from './components/CadastroRegistroDeTrabalhoView';
import { IdentificacaoFacialView } from './components/IdentificacaoFacialView';
import { AuditoriaView } from './components/AuditoriaView';

export const App: React.FC = () => {
  const [adminLogado, setAdminLogado] = useState<Administrador | null>(() =>
    carregarSessaoAdmin()
  );
  const [telaLogin, setTelaLogin] = useState<'login' | 'redefinir'>('login');
  const [viewAtual, setViewAtual] = useState<string>('dashboard');
  const [apenadoSelecionado, setApenadoSelecionado] = useState<Usuario | null>(null);
  const [usuarioParaEditar, setUsuarioParaEditar] = useState<Usuario | null>(null);
  const [idUsuarioPreSelecionado, setIdUsuarioPreSelecionado] = useState<number | undefined>();
  const [sidebarCollapsed, setSidebarCollapsed] = useState<boolean>(false);
  const [termoBuscaGlobal, setTermoBuscaGlobal] = useState<string>('');

  useEffect(() => {
    initApiBaseUrl();
    setOnUnauthorizedHandler(() => {
      setAdminLogado(null);
      setViewAtual('dashboard');
    });
    return () => {
      setOnUnauthorizedHandler(null);
    };
  }, []);

  const handleLoginSuccess = (admin: Administrador) => {
    setAdminLogado(admin);
    setViewAtual('dashboard');
  };

  const handleLogout = () => {
    limparSessaoAdmin();
    setAdminLogado(null);
    setViewAtual('dashboard');
  };

  const handleNavigate = (view: string, idParam?: number) => {
    if (view === 'usuarios' && idParam === -1) {
      setUsuarioParaEditar(null);
    }
    setViewAtual(view);
  };

  const handleSelecionarApenado = (u: Usuario) => {
    setApenadoSelecionado(u);
    setViewAtual('detalhe');
  };

  const handleEditarApenado = (u: Usuario) => {
    setUsuarioParaEditar(u);
    setViewAtual('usuarios');
  };

  const handleNovoPonto = (idUsuario: number) => {
    setIdUsuarioPreSelecionado(idUsuario);
    setViewAtual('pontos');
  };

  const handleNovaPena = (idUsuario: number) => {
    setIdUsuarioPreSelecionado(idUsuario);
    setViewAtual('penas');
  };

  const handleBuscarGlobalSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const termo = termoBuscaGlobal.trim().toLowerCase();
    const termoNum = termo.replace(/\D/g, '');

    if (!termo) return;

    try {
      const todos = await api.get<Usuario[]>('/usuarios');
      const encontrado = todos.find((u) => {
        const cpfLimpo = (u.cpf || '').replace(/\D/g, '');
        if (termoNum && cpfLimpo.includes(termoNum)) return true;
        if (u.nome && u.nome.toLowerCase().includes(termo)) return true;
        if (u.codigo && u.codigo.toLowerCase().includes(termo)) return true;
        return false;
      });

      if (encontrado) {
        handleSelecionarApenado(encontrado);
      } else {
        setViewAtual('dashboard');
      }
    } catch {
      setViewAtual('dashboard');
    }
  };

  if (!adminLogado) {
    if (telaLogin === 'redefinir') {
      return <RedefinirSenhaView onVoltar={() => setTelaLogin('login')} />;
    }
    return (
      <LoginView
        onLoginSuccess={handleLoginSuccess}
        onNavigateRedefinir={() => setTelaLogin('redefinir')}
      />
    );
  }

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-slate-50 font-sans text-slate-800 antialiased">
      {/* SaaS Sidebar */}
      <Sidebar
        admin={adminLogado}
        currentView={viewAtual}
        onNavigate={handleNavigate}
        onLogout={handleLogout}
        collapsed={sidebarCollapsed}
        onToggleCollapse={() => setSidebarCollapsed(!sidebarCollapsed)}
      />

      {/* Main Workspace with Topbar */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden bg-slate-50/60">
        <HeaderSaaS
          currentView={viewAtual}
          onNavigate={handleNavigate}
          termoBusca={termoBuscaGlobal}
          onTermoBuscaChange={setTermoBuscaGlobal}
          onBuscarSubmit={handleBuscarGlobalSubmit}
          subtitulo={
            viewAtual === 'detalhe' && apenadoSelecionado
              ? apenadoSelecionado.nome
              : undefined
          }
        />

        {/* Scrollable Main Content */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8">
          <div className="max-w-7xl mx-auto">
            {viewAtual === 'dashboard' && (
              <DashboardView
                onNavigate={handleNavigate}
                onSelecionarApenado={handleSelecionarApenado}
              />
            )}

            {viewAtual === 'detalhe' && apenadoSelecionado && (
              <DetalheApenadoView
                usuario={apenadoSelecionado}
                adminId={adminLogado.adminId}
                onVoltar={() => setViewAtual('dashboard')}
                onEditarUsuario={handleEditarApenado}
                onNovoPonto={handleNovoPonto}
                onNovaPena={handleNovaPena}
              />
            )}

            {viewAtual === 'usuarios' && (
              <CadastroUsuarioView
                adminId={adminLogado.adminId}
                usuarioParaEditar={usuarioParaEditar}
                onVoltar={() => setViewAtual('dashboard')}
                onSalvo={() => {
                  setUsuarioParaEditar(null);
                }}
              />
            )}

            {viewAtual === 'instituicoes' && (
              <CadastroInstituicaoView onVoltar={() => setViewAtual('dashboard')} />
            )}

            {viewAtual === 'penas' && (
              <CadastroPenaView
                idUsuarioPreSelecionado={idUsuarioPreSelecionado}
                onVoltar={() => setViewAtual('dashboard')}
                onSalvo={() => setViewAtual('dashboard')}
              />
            )}

            {viewAtual === 'pontos' && (
              <CadastroRegistroDeTrabalhoView
                idUsuarioPreSelecionado={idUsuarioPreSelecionado}
                onVoltar={() => setViewAtual('dashboard')}
                onSalvo={() => setViewAtual('dashboard')}
              />
            )}

            {viewAtual === 'facial' && (
              <IdentificacaoFacialView
                onVoltar={() => setViewAtual('dashboard')}
                onAbrirDetalhe={handleSelecionarApenado}
              />
            )}

            {viewAtual === 'auditoria' && (
              <AuditoriaView onVoltar={() => setViewAtual('dashboard')} />
            )}
          </div>
        </main>
      </div>
    </div>
  );
};
