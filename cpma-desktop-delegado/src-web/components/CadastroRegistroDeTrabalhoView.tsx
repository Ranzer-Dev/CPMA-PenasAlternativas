import React, { useState, useEffect } from 'react';
import { Usuario, Instituicao, Pena } from '../types';
import { formatarData, calcularHorasTrabalhadas } from '../utils/formatters';
import {
  Clock,
  Plus,
  Calendar,
  Trash2,
  Check,
  AlertCircle,
  Sparkles,
} from 'lucide-react';

interface RowTemp {
  id: string;
  data: string;
  inicio: string;
  almoco: string;
  volta: string;
  saida: string;
  horas: number;
  atividades: string;
}

interface CadastroRegistroDeTrabalhoViewProps {
  idUsuarioPreSelecionado?: number;
  onVoltar: () => void;
}

export const CadastroRegistroDeTrabalhoView: React.FC<CadastroRegistroDeTrabalhoViewProps> = ({
  idUsuarioPreSelecionado,
  onVoltar,
}) => {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [penas, setPenas] = useState<Pena[]>([]);
  const [instituicoes, setInstituicoes] = useState<Instituicao[]>([]);

  // Selection
  const [idUsuario, setIdUsuario] = useState<number>(idUsuarioPreSelecionado || 0);
  const [idPena, setIdPena] = useState<number>(0);
  const [idInstituicao, setIdInstituicao] = useState<number>(0);

  // Table of records being registered
  const [linhas, setLinhas] = useState<RowTemp[]>([
    {
      id: '1',
      data: new Date().toISOString().split('T')[0],
      inicio: '08:00',
      almoco: '12:00',
      volta: '13:00',
      saida: '17:00',
      horas: 8,
      atividades: 'Pintura, limpeza e manutenção da área comum.',
    },
  ]);

  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [sucesso, setSucesso] = useState<string | null>(null);

  const carregarDados = async () => {
    try {
      const resUsers = await fetch('/api/v1/usuarios');
      if (resUsers.ok) {
        const uList: Usuario[] = await resUsers.json();
        setUsuarios(uList);
        if (!idUsuario && uList.length > 0) {
          setIdUsuario(idUsuarioPreSelecionado || uList[0].id_usuario);
        }
      }

      const resInst = await fetch('/api/v1/instituicoes');
      if (resInst.ok) {
        const iList: Instituicao[] = await resInst.json();
        setInstituicoes(iList);
        if (iList.length > 0) setIdInstituicao(iList[0].id_instituicao);
      }
    } catch (e) {
      console.warn('Erro ao carregar dados:', e);
    }
  };

  useEffect(() => {
    carregarDados();
  }, []);

  // When user is selected, load their penalties
  useEffect(() => {
    if (idUsuario) {
      fetch(`/api/v1/penas/usuario/${idUsuario}`)
        .then(res => res.json())
        .then((pList: Pena[]) => {
          setPenas(pList);
          if (pList.length > 0) {
            setIdPena(pList[0].id_pena);
            setIdInstituicao(pList[0].fk_instituicao_id_instituicao);
          } else {
            setIdPena(0);
          }
        })
        .catch(console.error);
    }
  }, [idUsuario]);

  const handleAdicionarDia = () => {
    const novaData = new Date().toISOString().split('T')[0];
    const novaLinha: RowTemp = {
      id: String(Date.now()),
      data: novaData,
      inicio: '08:00',
      almoco: '',
      volta: '',
      saida: '12:00',
      horas: 4,
      atividades: 'Cumprimento de atividades rotineiras.',
    };
    setLinhas(prev => [...prev, novaLinha]);
  };

  const handleAdicionarMes = () => {
    // Generate dates for current month (Mon, Wed, Fri by default)
    const agora = new Date();
    const ano = agora.getFullYear();
    const mes = agora.getMonth();
    const diasNoMes = new Date(ano, mes + 1, 0).getDate();

    const novas: RowTemp[] = [];
    for (let d = 1; d <= diasNoMes; d++) {
      const dataObj = new Date(ano, mes, d);
      const diaSemanaNum = dataObj.getDay(); // 1 = Mon, 3 = Wed
      if (diaSemanaNum === 1 || diaSemanaNum === 3) {
        const iso = dataObj.toISOString().split('T')[0];
        novas.push({
          id: `mes-${d}`,
          data: iso,
          inicio: '08:00',
          almoco: '',
          volta: '',
          saida: '12:00',
          horas: 4,
          atividades: 'Prestação de serviços comunitários na instituição parceira.',
        });
      }
    }

    if (novas.length > 0) {
      setLinhas(novas);
      setSucesso(`Grade mensal gerada com ${novas.length} dias programados!`);
    }
  };

  const handleRemoverLinha = (id: string) => {
    setLinhas(prev => prev.filter(l => l.id !== id));
  };

  const handleAtualizarLinha = (id: string, campo: keyof RowTemp, valor: any) => {
    setLinhas(prev =>
      prev.map(l => {
        if (l.id !== id) return l;
        const atualizada = { ...l, [campo]: valor };
        // Recalculate hours if time changed
        if (['inicio', 'almoco', 'volta', 'saida'].includes(campo)) {
          atualizada.horas = calcularHorasTrabalhadas(
            atualizada.inicio,
            atualizada.almoco,
            atualizada.volta,
            atualizada.saida
          );
        }
        return atualizada;
      })
    );
  };

  const totalHorasCalculadas = linhas.reduce((acc, l) => acc + (Number(l.horas) || 0), 0);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro(null);
    setSucesso(null);

    if (!idPena) {
      setErro('Selecione uma pena ativa para lançar os registros de trabalho.');
      return;
    }
    if (linhas.length === 0) {
      setErro('Adicione pelo menos um dia de registro na folha.');
      return;
    }

    setSalvando(true);
    try {
      const payload = linhas.map(l => ({
        data_trabalho: l.data,
        horas_cumpridas: l.horas,
        atividades: l.atividades,
        horario_inicio: l.inicio,
        horario_almoco: l.almoco || null,
        horario_volta: l.volta || null,
        horario_saida: l.saida,
        fk_pena_id_pena: idPena,
        fk_instituicao_id_instituicao: idInstituicao || null,
      }));

      const res = await fetch('/api/v1/registros-trabalho/lote', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.error || 'Erro ao registrar frequência de trabalho.');
      }

      setSucesso(`Sucesso! ${linhas.length} registros de trabalho lançados (${totalHorasCalculadas}h).`);
      setLinhas([]);
    } catch (err: any) {
      setErro(err.message || 'Falha ao salvar registros.');
    } finally {
      setSalvando(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 flex items-center space-x-2">
            <Clock className="w-6 h-6 text-purple-600" />
            <span>Lançamento de Registro de Trabalho (Folha de Ponto)</span>
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Controle de horas cumpridas, intervalos e atividades desenvolvidas pelo apenado
          </p>
        </div>
        <button
          onClick={onVoltar}
          className="px-4 py-2 border border-slate-300 rounded-lg text-xs font-semibold text-slate-700 hover:bg-slate-50 cursor-pointer self-start sm:self-auto"
        >
          Voltar
        </button>
      </div>

      {erro && (
        <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg flex items-center space-x-2">
          <AlertCircle className="w-4 h-4 shrink-0 text-red-600" />
          <span>{erro}</span>
        </div>
      )}

      {sucesso && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-lg flex items-center space-x-2">
          <Check className="w-4 h-4 shrink-0 text-emerald-600" />
          <span>{sucesso}</span>
        </div>
      )}

      {/* Selectors Card */}
      <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div>
          <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
            Apenado *
          </label>
          <select
            value={idUsuario}
            onChange={(e) => setIdUsuario(Number(e.target.value))}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-purple-500 focus:outline-hidden"
          >
            <option value={0}>Selecione um apenado...</option>
            {usuarios.map((u) => (
              <option key={u.id_usuario} value={u.id_usuario}>
                {u.nome} ({u.codigo})
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
            Pena Vinculada *
          </label>
          <select
            value={idPena}
            onChange={(e) => setIdPena(Number(e.target.value))}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-purple-500 focus:outline-hidden"
          >
            <option value={0}>
              {penas.length === 0 ? 'Nenhuma pena ativa para este apenado' : 'Selecione a pena...'}
            </option>
            {penas.map((p) => (
              <option key={p.id_pena} value={p.id_pena}>
                {p.tipo_pena} ({p.horas_totais}h totais)
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
            Instituição do Ponto *
          </label>
          <select
            value={idInstituicao}
            onChange={(e) => setIdInstituicao(Number(e.target.value))}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-purple-500 focus:outline-hidden"
          >
            {instituicoes.map((i) => (
              <option key={i.id_instituicao} value={i.id_instituicao}>
                {i.nome}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Grid of Lines (Table) */}
      <div className="bg-white rounded-2xl p-6 sm:p-8 border border-slate-200 shadow-sm space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
          <div>
            <h3 className="font-bold text-slate-900 text-sm">Lançamentos de Horas</h3>
            <p className="text-xs text-slate-500">Adicione dias avulsos ou gere a grade completa do mês</p>
          </div>

          {/* Action buttons mirroring JavaFX buttons */}
          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={handleAdicionarDia}
              className="flex items-center space-x-1 px-3 py-1.5 bg-purple-600 hover:bg-purple-700 text-white rounded-lg text-xs font-bold transition-colors cursor-pointer"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>+ Adicionar Dia</span>
            </button>
            <button
              type="button"
              onClick={handleAdicionarMes}
              className="flex items-center space-x-1 px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold transition-colors cursor-pointer"
            >
              <Calendar className="w-3.5 h-3.5" />
              <span>Adicionar Mês</span>
            </button>
            <button
              type="button"
              onClick={() => setLinhas([])}
              className="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-xs font-bold border border-slate-300 transition-colors cursor-pointer"
            >
              Limpar Grade
            </button>
          </div>
        </div>

        {linhas.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-slate-50 text-slate-700 uppercase font-bold border-b border-slate-200">
                  <th className="py-2.5 px-3">Data</th>
                  <th className="py-2.5 px-3">Entrada</th>
                  <th className="py-2.5 px-3">Almoço</th>
                  <th className="py-2.5 px-3">Retorno</th>
                  <th className="py-2.5 px-3">Saída</th>
                  <th className="py-2.5 px-3">Horas</th>
                  <th className="py-2.5 px-3">Atividades</th>
                  <th className="py-2.5 px-2 text-center">Ação</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {linhas.map((linha) => (
                  <tr key={linha.id} className="hover:bg-slate-50">
                    <td className="py-2 px-3">
                      <input
                        type="date"
                        value={linha.data}
                        onChange={(e) => handleAtualizarLinha(linha.id, 'data', e.target.value)}
                        className="px-2 py-1 border border-slate-300 rounded text-xs bg-white"
                      />
                    </td>
                    <td className="py-2 px-3">
                      <input
                        type="time"
                        value={linha.inicio}
                        onChange={(e) => handleAtualizarLinha(linha.id, 'inicio', e.target.value)}
                        className="px-2 py-1 border border-slate-300 rounded text-xs bg-white w-20"
                      />
                    </td>
                    <td className="py-2 px-3">
                      <input
                        type="time"
                        value={linha.almoco}
                        onChange={(e) => handleAtualizarLinha(linha.id, 'almoco', e.target.value)}
                        className="px-2 py-1 border border-slate-300 rounded text-xs bg-white w-20"
                      />
                    </td>
                    <td className="py-2 px-3">
                      <input
                        type="time"
                        value={linha.volta}
                        onChange={(e) => handleAtualizarLinha(linha.id, 'volta', e.target.value)}
                        className="px-2 py-1 border border-slate-300 rounded text-xs bg-white w-20"
                      />
                    </td>
                    <td className="py-2 px-3">
                      <input
                        type="time"
                        value={linha.saida}
                        onChange={(e) => handleAtualizarLinha(linha.id, 'saida', e.target.value)}
                        className="px-2 py-1 border border-slate-300 rounded text-xs bg-white w-20"
                      />
                    </td>
                    <td className="py-2 px-3 font-bold text-emerald-600 whitespace-nowrap">
                      {linha.horas}h
                    </td>
                    <td className="py-2 px-3">
                      <input
                        type="text"
                        value={linha.atividades}
                        onChange={(e) => handleAtualizarLinha(linha.id, 'atividades', e.target.value)}
                        placeholder="Atividades realizadas"
                        className="px-2 py-1 border border-slate-300 rounded text-xs bg-white w-full min-w-[200px]"
                      />
                    </td>
                    <td className="py-2 px-2 text-center">
                      <button
                        type="button"
                        onClick={() => handleRemoverLinha(linha.id)}
                        className="p-1 text-slate-400 hover:text-red-600 rounded cursor-pointer"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-10 text-slate-400 text-xs">
            Nenhum dia na grade. Clique em "+ Adicionar Dia" ou "Adicionar Mês" acima.
          </div>
        )}

        {/* Total Summary Footer */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-4 border-t border-slate-100 bg-slate-50 p-4 rounded-xl">
          <div className="flex items-center space-x-3">
            <span className="text-xs font-bold text-slate-600 uppercase">Total a Registrar:</span>
            <span className="text-xl font-black text-purple-700 font-mono">
              {totalHorasCalculadas} horas
            </span>
            <span className="text-xs text-slate-500">({linhas.length} dias)</span>
          </div>

          <button
            type="button"
            onClick={handleSubmit}
            disabled={salvando || linhas.length === 0}
            className="w-full sm:w-auto px-6 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-xs transition-colors cursor-pointer disabled:opacity-50"
          >
            {salvando ? 'Salvando...' : 'Confirmar e Salvar Registros'}
          </button>
        </div>
      </div>
    </div>
  );
};
