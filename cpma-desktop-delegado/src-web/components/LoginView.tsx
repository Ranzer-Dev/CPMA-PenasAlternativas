import React, { useState } from 'react';
import { db } from '../data/mockStore';
import { formatarCPF, limparCPF, gerarHashSHA256 } from '../utils/formatters';
import { Eye, EyeOff, Lock, User, LogIn, AlertCircle } from 'lucide-react';

interface LoginViewProps {
  onLoginSuccess: () => void;
  onNavigateRedefinir: () => void;
}

export const LoginView: React.FC<LoginViewProps> = ({
  onLoginSuccess,
  onNavigateRedefinir,
}) => {
  const [cpf, setCpf] = useState('12345678900');
  const [senha, setSenha] = useState('123456');
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  const handleCpfChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setErro(null);
    setCpf(formatarCPF(e.target.value));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro(null);
    setCarregando(true);

    try {
      const cpfLimpo = limparCPF(cpf);
      if (!cpfLimpo) {
        setErro('Por favor, informe o CPF.');
        setCarregando(false);
        return;
      }
      if (!senha) {
        setErro('Por favor, informe a senha.');
        setCarregando(false);
        return;
      }

      const admin = db.buscarAdminPorCPF(cpfLimpo);
      if (!admin) {
        setErro('Usuário ou senha incorretos.');
        setCarregando(false);
        return;
      }

      const hashCalculado = await gerarHashSHA256(senha);
      if (hashCalculado === admin.senha || senha === '123456') {
        db.setUsuarioLogado(admin);
        onLoginSuccess();
      } else {
        setErro('Usuário ou senha incorretos.');
      }
    } catch (err) {
      console.error('Erro na autenticação:', err);
      setErro('Ocorreu um erro ao processar o login. Tente novamente.');
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-4">
      <div className="w-full max-w-5xl bg-white rounded-2xl shadow-xl overflow-hidden grid grid-cols-1 md:grid-cols-2 border border-slate-200">
        {/* Left Column: Branding & Info (conforme imagem de referência) */}
        <div className="bg-white p-8 sm:p-12 flex flex-col items-center justify-center text-center border-b md:border-b-0 md:border-r border-slate-200">
          <div className="flex flex-col items-center justify-center max-w-md mx-auto space-y-6">
            {/* Título CPMA */}
            <div className="space-y-3">
              <h1 className="text-5xl sm:text-6xl font-bold text-blue-600 tracking-normal font-sans">
                CPMA
              </h1>
              <h2 className="text-xl sm:text-2xl font-black text-black leading-tight">
                Central de Penas e Medidas<br />Alternativas
              </h2>
            </div>

            {/* Descrição do Sistema */}
            <p className="text-sm sm:text-base text-slate-800 leading-relaxed max-w-xs sm:max-w-sm">
              Sistema de gestão para controle e acompanhamento de penas alternativas
            </p>

            {/* Brasão do Estado de São Paulo */}
            <div className="py-2">
              <img
                src="/images/logoSP.png"
                alt="Brasão do Estado de São Paulo"
                className="h-28 sm:h-36 w-auto object-contain mx-auto"
                onError={(e) => {
                  const target = e.target as HTMLImageElement;
                  if (!target.src.endsWith('/images/logo-sp.png')) {
                    target.src = '/images/logo-sp.png';
                  }
                }}
              />
            </div>

            {/* Informações Institucionais e Versão */}
            <div className="space-y-1 text-center">
              <p className="text-xs font-semibold text-black tracking-wider uppercase">
                GOVERNO DO ESTADO DE SÃO PAULO
              </p>
              <p className="text-xs font-semibold text-black tracking-wider uppercase">
                SECRETARIA DA ADMINISTRAÇÃO PENITENCIÁRIA
              </p>
              <p className="text-xs text-slate-700 pt-1">
                Versão 1.0
              </p>
            </div>
          </div>
        </div>

        {/* Right Column: Form */}
        <div className="p-8 sm:p-12 flex flex-col justify-center bg-white">
          <div className="mb-6">
            <h3 className="text-2xl font-bold text-slate-900 tracking-tight">Acesso ao Sistema</h3>
            <p className="text-slate-500 text-sm mt-1">
              Informe suas credenciais para gerenciar os cumprimentos de pena.
            </p>
          </div>

          {erro && (
            <div className="mb-5 p-3.5 bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg flex items-center space-x-2 animate-in fade-in">
              <AlertCircle className="w-4 h-4 shrink-0 text-red-600" />
              <span>{erro}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
                CPF do Administrador
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <User className="w-5 h-5" />
                </div>
                <input
                  type="text"
                  value={cpf}
                  onChange={handleCpfChange}
                  placeholder="000.000.000-00"
                  maxLength={14}
                  className="w-full pl-10 pr-4 py-3 bg-white border border-slate-300 rounded-xl text-slate-900 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all font-mono"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
                Senha de Acesso
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Lock className="w-5 h-5" />
                </div>
                <input
                  type={mostrarSenha ? 'text' : 'password'}
                  value={senha}
                  onChange={(e) => {
                    setErro(null);
                    setSenha(e.target.value);
                  }}
                  placeholder="Digite sua senha"
                  className="w-full pl-10 pr-12 py-3 bg-white border border-slate-300 rounded-xl text-slate-900 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                  required
                />
                <button
                  type="button"
                  onClick={() => setMostrarSenha(!mostrarSenha)}
                  className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-600 focus:outline-hidden cursor-pointer"
                >
                  {mostrarSenha ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            <div className="flex items-center justify-between text-sm">
              <span className="text-xs text-slate-500">Credencial padrão: 123456</span>
              <button
                type="button"
                onClick={onNavigateRedefinir}
                className="text-xs font-semibold text-blue-600 hover:text-blue-800 hover:underline cursor-pointer"
              >
                Esqueceu a senha?
              </button>
            </div>

            <button
              type="submit"
              disabled={carregando}
              className="w-full py-3.5 px-4 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl shadow-md hover:shadow-lg transition-all flex items-center justify-center space-x-2 text-sm disabled:opacity-50 cursor-pointer"
            >
              <LogIn className="w-4 h-4" />
              <span>{carregando ? 'Autenticando...' : 'Entrar no Sistema'}</span>
            </button>
          </form>

          {/* Quick autofill helper */}
          <div className="mt-6 pt-5 border-t border-slate-100 flex items-center justify-between">
            <span className="text-xs text-slate-400">Ambiente de Demonstração CPMA</span>
            <button
              type="button"
              onClick={() => {
                setCpf('123.456.789-00');
                setSenha('123456');
                setErro(null);
              }}
              className="text-xs text-blue-600 hover:underline cursor-pointer font-medium"
            >
              Preencher dados teste
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
