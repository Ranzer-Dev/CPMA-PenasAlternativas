import React, { useState, useEffect } from 'react';
import { Usuario, Pena, RegistroDeTrabalho } from '../types';
import { formatarCPF, formatarData, formatarTelefone } from '../utils/formatters';
import {
  ArrowLeft,
  Printer,
  Edit,
  Clock,
  KeyRound,
  CheckCircle,
  AlertTriangle,
  User,
  Calendar,
  Building,
} from 'lucide-react';

interface DetalheApenadoViewProps {
  usuario: Usuario;
  onVoltar: () => void;
  onEditarUsuario: (u: Usuario) => void;
  onNovoPonto: (idUsuario: number) => void;
  onNovaPena: (idUsuario: number) => void;
}

export const DetalheApenadoView: React.FC<DetalheApenadoViewProps> = ({
  usuario,
  onVoltar,
  onEditarUsuario,
  onNovoPonto,
  onNovaPena,
}) => {
  const [penas, setPenas] = useState<Pena[]>([]);
  const [penaAtiva, setPenaAtiva] = useState<Pena | null>(null);
  const [registros, setRegistros] = useState<RegistroDeTrabalho[]>([]);
  const [resumo, setResumo] = useState<{
    horasCumpridas: number;
    horasTotais: number;
    horasFaltantes: number;
    percentualCumprido: number;
  }>({
    horasCumpridas: 0,
    horasTotais: 0,
    horasFaltantes: 0,
    percentualCumprido: 0,
  });

  const [codigoTotemGerado, setCodigoTotemGerado] = useState<string | null>(null);
  const [gerandoCodigo, setGerandoCodigo] = useState(false);

  const carregarDadosPena = async () => {
    try {
      const resPenas = await fetch(`/api/v1/penas/usuario/${usuario.id_usuario}`);
      if (resPenas.ok) {
        const dataPenas: Pena[] = await resPenas.json();
        setPenas(dataPenas);
        if (dataPenas.length > 0) {
          const ultimaPena = dataPenas[0];
          setPenaAtiva(ultimaPena);

          // Carregar registros e resumo
          const resReg = await fetch(`/api/v1/registros-trabalho/pena/${ultimaPena.id_pena}`);
          if (resReg.ok) {
            const dataReg = await resReg.json();
            setRegistros(dataReg);
          }

          const resResumo = await fetch(`/api/v1/registros-trabalho/pena/${ultimaPena.id_pena}/resumo`);
          if (resResumo.ok) {
            const dataResumo = await resResumo.json();
            setResumo(dataResumo);
          }
        }
      }
    } catch (e) {
      console.warn('Erro ao carregar dados da pena:', e);
    }
  };

  useEffect(() => {
    carregarDadosPena();
  }, [usuario.id_usuario]);

  const handleGerarCodigoTotem = async () => {
    setGerandoCodigo(true);
    try {
      const res = await fetch('/api/v1/totem/codigo-temporario', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          idUsuario: usuario.id_usuario,
          idAdmin: 1,
        }),
      });

      if (res.ok) {
        const data = await res.json();
        setCodigoTotemGerado(data.codigo);
      }
    } catch (e) {
      console.error('Erro ao gerar código do totem:', e);
    } finally {
      setGerandoCodigo(false);
    }
  };

  const handleImprimir = () => {
    window.print();
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-6">
      {/* Top Bar Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 print:hidden">
        <button
          onClick={onVoltar}
          className="flex items-center space-x-1.5 text-xs font-semibold text-slate-600 hover:text-slate-900 bg-white border border-slate-300 px-3 py-2 rounded-lg shadow-2xs hover:bg-slate-50 transition-colors w-fit cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Voltar à Busca</span>
        </button>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={handleGerarCodigoTotem}
            disabled={gerandoCodigo}
            className="flex items-center space-x-1.5 px-3 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-lg shadow-xs transition-colors cursor-pointer"
          >
            <KeyRound className="w-3.5 h-3.5" />
            <span>{gerandoCodigo ? 'Gerando...' : 'Gerar PIN Totem'}</span>
          </button>

          <button
            onClick={() => onNovoPonto(usuario.id_usuario)}
            className="flex items-center space-x-1.5 px-3 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-lg shadow-xs transition-colors cursor-pointer"
          >
            <Clock className="w-3.5 h-3.5" />
            <span>Lançar Ponto</span>
          </button>

          <button
            onClick={() => onEditarUsuario(usuario)}
            className="flex items-center space-x-1.5 px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-lg border border-slate-300 transition-colors cursor-pointer"
          >
            <Edit className="w-3.5 h-3.5" />
            <span>Editar Ficha</span>
          </button>

          <button
            onClick={handleImprimir}
            className="flex items-center space-x-1.5 px-3 py-2 bg-slate-800 hover:bg-slate-900 text-white text-xs font-bold rounded-lg shadow-xs transition-colors cursor-pointer"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>Imprimir Ficha</span>
          </button>
        </div>
      </div>

      {/* PIN Code Generated Notification Modal */}
      {codigoTotemGerado && (
        <div className="bg-indigo-50 border-2 border-indigo-300 rounded-2xl p-5 flex flex-col sm:flex-row items-center justify-between gap-4 animate-in fade-in">
          <div className="flex items-center space-x-4">
            <div className="w-12 h-12 rounded-xl bg-indigo-600 text-white flex items-center justify-center shrink-0">
              <KeyRound className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xs font-bold text-indigo-700 uppercase tracking-wide">
                Senha de 6 números gerada para o Totem
              </span>
              <p className="text-xs text-slate-600">
                O apenado deve digitar este código no teclado virtual do terminal de autoatendimento. Válido por 24h.
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-3">
            <div className="px-5 py-2.5 bg-white border-2 border-indigo-400 rounded-xl font-mono text-2xl font-black text-indigo-700 tracking-widest shadow-inner">
              {codigoTotemGerado}
            </div>
            <button
              onClick={() => setCodigoTotemGerado(null)}
              className="text-xs text-slate-500 hover:text-slate-800 font-semibold px-2 py-1 cursor-pointer"
            >
              Fechar
            </button>
          </div>
        </div>
      )}

      {/* Main Profile Header Card */}
      <div className="bg-white rounded-2xl p-6 sm:p-8 border border-slate-200 shadow-sm grid grid-cols-1 md:grid-cols-4 gap-6">
        {/* Photo and Badge */}
        <div className="flex flex-col items-center justify-center text-center space-y-3 md:border-r md:border-slate-100 md:pr-6">
          <div className="w-36 h-36 rounded-2xl bg-slate-100 border-2 border-slate-200 overflow-hidden shadow-xs flex items-center justify-center">
            {usuario.foto ? (
              <img src={usuario.foto} alt={usuario.nome} className="w-full h-full object-cover" />
            ) : (
              <User className="w-16 h-16 text-slate-400" />
            )}
          </div>
          <div>
            <span className="px-2.5 py-1 bg-blue-100 text-blue-800 rounded-full text-xs font-black uppercase tracking-wider">
              {usuario.codigo || 'PENA-001'}
            </span>
          </div>
        </div>

        {/* Info Grid */}
        <div className="md:col-span-3 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 text-sm">
          <div>
            <label className="text-xs font-bold text-slate-400 uppercase">Nome Completo</label>
            <p className="font-bold text-slate-900 text-base">{usuario.nome}</p>
          </div>
          <div>
            <label className="text-xs font-bold text-slate-400 uppercase">CPF</label>
            <p className="font-mono font-bold text-slate-800">{formatarCPF(usuario.cpf)}</p>
          </div>
          <div>
            <label className="text-xs font-bold text-slate-400 uppercase">Data de Nascimento</label>
            <p className="text-slate-800 font-medium">{formatarData(usuario.data_nascimento)}</p>
          </div>
          <div>
            <label className="text-xs font-bold text-slate-400 uppercase">Telefone / Contato</label>
            <p className="text-slate-800 font-medium">{formatarTelefone(usuario.telefone) || 'Não informado'}</p>
          </div>
          <div>
            <label className="text-xs font-bold text-slate-400 uppercase">Endereço Residencial</label>
            <p className="text-slate-800 font-medium">{usuario.endereco || 'Não informado'}</p>
          </div>
          <div>
            <label className="text-xs font-bold text-slate-400 uppercase">Bairro / Município</label>
            <p className="text-slate-800 font-medium">
              {usuario.bairro ? `${usuario.bairro}, ` : ''}{usuario.cidade || 'Votorantim'} - {usuario.uf || 'SP'}
            </p>
          </div>
          <div>
            <label className="text-xs font-bold text-slate-400 uppercase">CEP</label>
            <p className="text-slate-800 font-medium">{usuario.cep || '-'}</p>
          </div>
          <div>
            <label className="text-xs font-bold text-slate-400 uppercase">Nacionalidade</label>
            <p className="text-slate-800 font-medium">{usuario.nacionalidade || 'Brasileira'}</p>
          </div>
          <div>
            <label className="text-xs font-bold text-slate-400 uppercase">Data de Cadastro</label>
            <p className="text-slate-800 font-medium">{formatarData(usuario.criado_em)}</p>
          </div>
          {usuario.observacao && (
            <div className="sm:col-span-2 lg:col-span-3 bg-slate-50 p-3 rounded-xl border border-slate-200 mt-2">
              <label className="text-xs font-bold text-slate-500 uppercase">Observações do Cadastro</label>
              <p className="text-slate-700 text-xs mt-0.5 leading-relaxed">{usuario.observacao}</p>
            </div>
          )}
        </div>
      </div>

      {/* Penalty Execution & Progress Summary */}
      {penaAtiva ? (
        <div className="bg-white rounded-2xl p-6 sm:p-8 border border-slate-200 shadow-sm space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-100 pb-4">
            <div>
              <span className="text-xs font-bold text-blue-600 uppercase tracking-wider">
                Execução Penal Vigente
              </span>
              <h3 className="text-xl font-bold text-slate-900">{penaAtiva.tipo_pena}</h3>
              <p className="text-xs text-slate-500 mt-0.5">{penaAtiva.descricao || 'Processo em andamento'}</p>
            </div>
            <div className="flex items-center space-x-2">
              <span className="px-3 py-1 bg-slate-100 text-slate-700 rounded-lg text-xs font-semibold">
                Início: {formatarData(penaAtiva.data_inicio)}
              </span>
              {penaAtiva.data_termino && (
                <span className="px-3 py-1 bg-slate-100 text-slate-700 rounded-lg text-xs font-semibold">
                  Término Previsto: {formatarData(penaAtiva.data_termino)}
                </span>
              )}
            </div>
          </div>

          {/* Progress Bar & Hours Cards */}
          <div className="space-y-3">
            <div className="flex justify-between text-xs font-bold text-slate-700">
              <span>Progresso da Pena: {resumo.percentualCumprido}%</span>
              <span>{resumo.horasCumpridas}h cumpridas de {resumo.horasTotais}h totais</span>
            </div>
            <div className="w-full h-4 bg-slate-100 rounded-full overflow-hidden border border-slate-200">
              <div
                className="h-full bg-linear-to-r from-blue-600 to-emerald-500 transition-all duration-500 rounded-full"
                style={{ width: `${Math.min(100, resumo.percentualCumprido)}%` }}
              ></div>
            </div>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 pt-2">
            <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 text-center">
              <label className="text-2xs font-bold text-slate-500 uppercase">Horas Totais</label>
              <p className="text-2xl font-black text-slate-900 mt-1">{resumo.horasTotais}h</p>
            </div>
            <div className="bg-emerald-50 p-4 rounded-xl border border-emerald-200 text-center">
              <label className="text-2xs font-bold text-emerald-700 uppercase">Cumpridas</label>
              <p className="text-2xl font-black text-emerald-700 mt-1">{resumo.horasCumpridas}h</p>
            </div>
            <div className="bg-amber-50 p-4 rounded-xl border border-amber-200 text-center">
              <label className="text-2xs font-bold text-amber-700 uppercase">Restantes</label>
              <p className="text-2xl font-black text-amber-700 mt-1">{resumo.horasFaltantes}h</p>
            </div>
            <div className="bg-blue-50 p-4 rounded-xl border border-blue-200 text-center">
              <label className="text-2xs font-bold text-blue-700 uppercase">Carga Semanal</label>
              <p className="text-2xl font-black text-blue-700 mt-1">{penaAtiva.horas_semanais}h/sem</p>
            </div>
          </div>

          {/* Agreed Activities */}
          {penaAtiva.atividades_acordadas && (
            <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 text-xs">
              <span className="font-bold text-slate-700 uppercase block mb-1">Atividades Acordadas:</span>
              <p className="text-slate-600">{penaAtiva.atividades_acordadas}</p>
            </div>
          )}
        </div>
      ) : (
        <div className="bg-white rounded-2xl p-8 border border-slate-200 text-center space-y-4">
          <AlertTriangle className="w-10 h-10 text-amber-500 mx-auto" />
          <div>
            <h4 className="text-base font-bold text-slate-900">Nenhuma pena cadastrada para este apenado</h4>
            <p className="text-xs text-slate-500 mt-1 max-w-md mx-auto">
              Cadastre a pena alternativa para associar a instituição parceira e iniciar o controle de frequência.
            </p>
          </div>
          <button
            onClick={() => onNovaPena(usuario.id_usuario)}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold cursor-pointer"
          >
            Cadastrar Nova Pena
          </button>
        </div>
      )}

      {/* Work Records Table mirroring JavaFX DetalheApenadoController tblRegistros */}
      <div className="bg-white rounded-2xl p-6 sm:p-8 border border-slate-200 shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h4 className="text-lg font-bold text-slate-900">Histórico de Registros de Trabalho</h4>
            <p className="text-xs text-slate-500">Folha de presença e controle de horas realizadas</p>
          </div>
          <span className="text-xs font-semibold px-2.5 py-1 bg-slate-100 text-slate-700 rounded-lg">
            {registros.length} registros
          </span>
        </div>

        {registros.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm border-collapse">
              <thead>
                <tr className="bg-slate-50 text-slate-700 text-xs font-bold uppercase border-b border-slate-200">
                  <th className="py-3 px-4">Data</th>
                  <th className="py-3 px-4">Horários</th>
                  <th className="py-3 px-4">Horas Cumpridas</th>
                  <th className="py-3 px-4">Atividades Realizadas</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {registros.map((r) => (
                  <tr key={r.id_registro} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4 font-semibold text-slate-900 whitespace-nowrap">
                      {formatarData(r.data_trabalho)}
                    </td>
                    <td className="py-3 px-4 text-xs font-mono text-slate-600 whitespace-nowrap">
                      {r.horario_inicio || '--:--'} às {r.horario_saida || '--:--'}
                      {r.horario_almoco && ` (Intervalo: ${r.horario_almoco}-${r.horario_volta})`}
                    </td>
                    <td className="py-3 px-4 font-bold text-emerald-600 whitespace-nowrap">
                      +{r.horas_cumpridas} horas
                    </td>
                    <td className="py-3 px-4 text-xs text-slate-600 max-w-md">
                      {r.atividades || 'Cumprimento de atividades rotineiras.'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-center py-8 text-xs text-slate-400">
            Nenhum registro de trabalho lançado ainda.
          </p>
        )}
      </div>
    </div>
  );
};
