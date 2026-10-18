import React, { useState } from 'react';
import { formatarCPF, limparCPF } from '../utils/formatters';
import { ArrowLeft, KeyRound, CheckCircle, AlertCircle } from 'lucide-react';

interface RedefinirSenhaViewProps {
  onVoltar: () => void;
}

export const RedefinirSenhaView: React.FC<RedefinirSenhaViewProps> = ({ onVoltar }) => {
  const [cpf, setCpf] = useState('123.456.789-00');
  const [respostaSecreta, setRespostaSecreta] = useState('Votorantim');
  const [novaSenha, setNovaSenha] = useState('');
  const [confirmaSenha, setConfirmaSenha] = useState('');
  const [sucesso, setSucesso] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro(null);

    if (novaSenha.length < 6) {
      setErro('A nova senha deve possuir pelo menos 6 caracteres.');
      return;
    }

    if (novaSenha !== confirmaSenha) {
      setErro('As senhas informadas não coincidem.');
      return;
    }

    setCarregando(true);
    try {
      const res = await fetch('/api/v1/auth/redefinir-senha', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          cpf: limparCPF(cpf),
          respostaSecreta,
          novaSenha,
        }),
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.error || 'Erro ao redefinir senha.');
      }

      setSucesso(true);
    } catch (err: any) {
      setErro(err.message || 'Falha ao redefinir senha. Verifique os dados informados.');
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-4">
      <div className="w-full max-w-lg bg-white rounded-2xl shadow-xl border border-slate-200 p-8">
        <button
          onClick={onVoltar}
          className="flex items-center space-x-1.5 text-xs font-semibold text-slate-500 hover:text-slate-800 mb-6 cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Voltar ao Login</span>
        </button>

        <div className="flex items-center space-x-3 mb-6">
          <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center">
            <KeyRound className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-slate-900">Redefinição de Senha</h2>
            <p className="text-xs text-slate-500">Recupere o acesso com a pergunta de segurança</p>
          </div>
        </div>

        {sucesso ? (
          <div className="text-center py-6 space-y-4">
            <div className="w-14 h-14 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto">
              <CheckCircle className="w-8 h-8" />
            </div>
            <h3 className="text-lg font-bold text-slate-900">Senha alterada com sucesso!</h3>
            <p className="text-sm text-slate-600 max-w-sm mx-auto">
              Sua nova credencial foi atualizada no sistema. Você já pode acessar com a nova senha.
            </p>
            <button
              onClick={onVoltar}
              className="mt-4 px-6 py-2.5 bg-blue-600 text-white rounded-xl text-sm font-semibold hover:bg-blue-700 cursor-pointer shadow-xs"
            >
              Ir para o Login
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {erro && (
              <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg flex items-center space-x-2">
                <AlertCircle className="w-4 h-4 shrink-0 text-red-600" />
                <span>{erro}</span>
              </div>
            )}

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                CPF do Administrador
              </label>
              <input
                type="text"
                value={cpf}
                onChange={(e) => setCpf(formatarCPF(e.target.value))}
                placeholder="000.000.000-00"
                className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm font-mono focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                required
              />
            </div>

            <div className="bg-slate-50 p-3.5 rounded-xl border border-slate-200 space-y-2">
              <p className="text-xs font-bold text-slate-600 uppercase">Pergunta de Segurança:</p>
              <p className="text-sm font-medium text-slate-800">"Qual a sua cidade natal?"</p>
              <div>
                <label className="block text-xs font-semibold text-slate-600 mt-2 mb-1">
                  Sua Resposta:
                </label>
                <input
                  type="text"
                  value={respostaSecreta}
                  onChange={(e) => setRespostaSecreta(e.target.value)}
                  placeholder="Digite sua resposta secreta"
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                Nova Senha
              </label>
              <input
                type="password"
                value={novaSenha}
                onChange={(e) => setNovaSenha(e.target.value)}
                placeholder="Mínimo 6 caracteres"
                className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                Confirmar Nova Senha
              </label>
              <input
                type="password"
                value={confirmaSenha}
                onChange={(e) => setConfirmaSenha(e.target.value)}
                placeholder="Repita a nova senha"
                className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                required
              />
            </div>

            <button
              type="submit"
              disabled={carregando}
              className="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl text-sm transition-all shadow-sm cursor-pointer disabled:opacity-50"
            >
              {carregando ? 'Atualizando...' : 'Redefinir Senha'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};
