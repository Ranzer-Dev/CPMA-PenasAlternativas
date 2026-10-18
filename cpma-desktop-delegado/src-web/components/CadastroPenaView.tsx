import React, { useState, useEffect } from 'react';
import { Usuario, Instituicao, Pena } from '../types';
import { formatarData } from '../utils/formatters';
import {
  Scale,
  Plus,
  Edit,
  Trash2,
  Calendar,
  Clock,
  Check,
  AlertCircle,
  FileText,
} from 'lucide-react';

interface CadastroPenaViewProps {
  idUsuarioPreSelecionado?: number;
  onVoltar: () => void;
}

export const CadastroPenaView: React.FC<CadastroPenaViewProps> = ({
  idUsuarioPreSelecionado,
  onVoltar,
}) => {
  const [penas, setPenas] = useState<Pena[]>([]);
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [instituicoes, setInstituicoes] = useState<Instituicao[]>([]);

  // Form State
  const [idPena, setIdPena] = useState<number | undefined>();
  const [idUsuario, setIdUsuario] = useState<number>(idUsuarioPreSelecionado || 0);
  const [idInstituicao, setIdInstituicao] = useState<number>(0);
  const [tipoPena, setTipoPena] = useState('Prestação de Serviços à Comunidade (PSC)');
  const [dataInicio, setDataInicio] = useState(new Date().toISOString().split('T')[0]);
  const [dataTermino, setDataTermino] = useState('');
  const [horasTotais, setHorasTotais] = useState<number>(120);
  const [horasSemanais, setHorasSemanais] = useState<number>(8);
  const [descricao, setDescricao] = useState('');
  const [atividadesAcordadas, setAtividadesAcordadas] = useState('');

  // Weekly Schedule
  const [diasSelecionados, setDiasSelecionados] = useState<string[]>([
    'Segunda-feira (08:00 às 12:00)',
    'Quarta-feira (08:00 às 12:00)',
  ]);

  const [estimativa, setEstimativa] = useState({
    semanas: 15,
    meses: 3.5,
  });

  const [erro, setErro] = useState<string | null>(null);
  const [sucesso, setSucesso] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  const carregarDados = async () => {
    try {
      const resPenas = await fetch('/api/v1/penas');
      if (resPenas.ok) setPenas(await resPenas.json());

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

  // Recalculate estimated duration and completion date whenever hours change
  useEffect(() => {
    if (horasTotais > 0 && horasSemanais > 0) {
      const semanas = Math.ceil(horasTotais / horasSemanais);
      const meses = Math.round((semanas / 4.33) * 10) / 10;
      setEstimativa({ semanas, meses });

      if (dataInicio) {
        const inicio = new Date(dataInicio);
        const dias = semanas * 7;
        const fim = new Date(inicio.getTime() + dias * 24 * 60 * 60 * 1000);
        setDataTermino(fim.toISOString().split('T')[0]);
      }
    }
  }, [horasTotais, horasSemanais, dataInicio]);

  const limparFormulario = () => {
    setIdPena(undefined);
    setTipoPena('Prestação de Serviços à Comunidade (PSC)');
    setDataInicio(new Date().toISOString().split('T')[0]);
    setDataTermino('');
    setHorasTotais(120);
    setHorasSemanais(8);
    setDescricao('');
    setAtividadesAcordadas('');
    setErro(null);
    setSucesso(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro(null);
    setSucesso(null);

    if (!idUsuario) {
      setErro('Selecione um apenado.');
      return;
    }
    if (!idInstituicao) {
      setErro('Selecione uma instituição parceira.');
      return;
    }

    setSalvando(true);
    try {
      const url = idPena ? `/api/v1/penas/${idPena}` : '/api/v1/penas';
      const method = idPena ? 'PUT' : 'POST';

      const payload = {
        tipo_pena: tipoPena,
        data_inicio: dataInicio,
        data_termino: dataTermino,
        descricao,
        dias_semana_e_horarios_disponivel: diasSelecionados.join('; '),
        atividades_acordadas: atividadesAcordadas,
        horas_semanais: Number(horasSemanais),
        tempo_pena: estimativa.semanas,
        horas_totais: Number(horasTotais),
        fk_usuario_id_usuario: Number(idUsuario),
        fk_instituicao_id_instituicao: Number(idInstituicao),
      };

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Erro ao salvar pena.');

      setSucesso(idPena ? 'Pena atualizada com sucesso!' : 'Pena cadastrada com sucesso!');
      await carregarDados();
      if (!idPena) limparFormulario();
    } catch (err: any) {
      setErro(err.message || 'Falha ao salvar pena.');
    } finally {
      setSalvando(false);
    }
  };

  const handleEditar = (p: Pena) => {
    setIdPena(p.id_pena);
    setIdUsuario(p.fk_usuario_id_usuario);
    setIdInstituicao(p.fk_instituicao_id_instituicao);
    setTipoPena(p.tipo_pena);
    setDataInicio(p.data_inicio);
    setDataTermino(p.data_termino || '');
    setHorasTotais(p.horas_totais);
    setHorasSemanais(p.horas_semanais);
    setDescricao(p.descricao || '');
    setAtividadesAcordadas(p.atividades_acordadas || '');
    setErro(null);
    setSucesso(null);
  };

  const handleExcluir = async (id: number) => {
    if (!confirm('Deseja excluir esta pena?')) return;
    try {
      const res = await fetch(`/api/v1/penas/${id}`, { method: 'DELETE' });
      if (res.ok) {
        await carregarDados();
        if (idPena === id) limparFormulario();
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 flex items-center space-x-2">
            <Scale className="w-6 h-6 text-rose-600" />
            <span>{idPena ? 'Editar Pena Alternativa' : 'Cadastro de Pena Alternativa'}</span>
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Vincule apenado e instituição, defina a carga horária e cronograma da execução penal
          </p>
        </div>
        <button
          onClick={onVoltar}
          className="px-4 py-2 border border-slate-300 rounded-lg text-xs font-semibold text-slate-700 hover:bg-slate-50 cursor-pointer self-start sm:self-auto"
        >
          Voltar
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Form (7 cols) */}
        <div className="lg:col-span-7 bg-white rounded-2xl p-6 sm:p-8 border border-slate-200 shadow-sm space-y-6">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-sm">Dados da Execução Penal</h3>
            {idPena && (
              <button
                onClick={limparFormulario}
                className="text-xs font-semibold text-rose-600 hover:underline cursor-pointer"
              >
                + Nova Pena
              </button>
            )}
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

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Apenado e Instituição */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Apenado Selecionado *
                </label>
                <select
                  value={idUsuario}
                  onChange={(e) => setIdUsuario(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-rose-500 focus:outline-hidden"
                  required
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
                  Instituição Conveniada *
                </label>
                <select
                  value={idInstituicao}
                  onChange={(e) => setIdInstituicao(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-rose-500 focus:outline-hidden"
                  required
                >
                  <option value={0}>Selecione a instituição...</option>
                  {instituicoes.map((i) => (
                    <option key={i.id_instituicao} value={i.id_instituicao}>
                      {i.nome} ({i.bairro || i.cidade})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Tipo de Pena */}
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                Tipo de Pena Alternativa *
              </label>
              <input
                type="text"
                value={tipoPena}
                onChange={(e) => setTipoPena(e.target.value)}
                placeholder="Ex: Prestação de Serviços à Comunidade (PSC)"
                className="w-full px-3.5 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-rose-500 focus:outline-hidden"
                required
              />
            </div>

            {/* Horas Totais, Horas Semanais, Duração Calculada */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Horas Totais *
                </label>
                <input
                  type="number"
                  min={1}
                  max={2000}
                  value={horasTotais}
                  onChange={(e) => setHorasTotais(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm font-bold text-slate-800 focus:ring-2 focus:ring-rose-500 focus:outline-hidden"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Horas Semanais *
                </label>
                <input
                  type="number"
                  min={1}
                  max={40}
                  value={horasSemanais}
                  onChange={(e) => setHorasSemanais(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm font-bold text-slate-800 focus:ring-2 focus:ring-rose-500 focus:outline-hidden"
                  required
                />
              </div>

              <div className="bg-rose-50/70 p-2.5 rounded-lg border border-rose-200 text-center flex flex-col justify-center">
                <span className="text-3xs font-bold text-rose-800 uppercase">Tempo Estimado</span>
                <p className="text-base font-black text-rose-700">
                  {estimativa.semanas} semanas ({estimativa.meses} meses)
                </p>
              </div>
            </div>

            {/* Datas */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Data de Início *
                </label>
                <input
                  type="date"
                  value={dataInicio}
                  onChange={(e) => setDataInicio(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-rose-500 focus:outline-hidden"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Término Previsto (calculado)
                </label>
                <input
                  type="date"
                  value={dataTermino}
                  onChange={(e) => setDataTermino(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-slate-50 text-slate-700 focus:ring-2 focus:ring-rose-500 focus:outline-hidden"
                />
              </div>
            </div>

            {/* Atividades Acordadas */}
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                Atividades Acordadas na Instituição
              </label>
              <textarea
                value={atividadesAcordadas}
                onChange={(e) => setAtividadesAcordadas(e.target.value)}
                rows={2}
                placeholder="Ex: Pintura, capina, apoio na organização do refeitório e jardinagem..."
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-rose-500 focus:outline-hidden"
              />
            </div>

            {/* Descrição / Processo */}
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                Dados do Processo / Observações Judiciais
              </label>
              <input
                type="text"
                value={descricao}
                onChange={(e) => setDescricao(e.target.value)}
                placeholder="Ex: Processo nº 0004521-82.2024.8.26.0663 - Vara Criminal"
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-rose-500 focus:outline-hidden"
              />
            </div>

            {/* Submit */}
            <div className="flex items-center justify-end space-x-3 pt-3">
              <button
                type="button"
                onClick={limparFormulario}
                className="px-4 py-2 border border-slate-300 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-50 cursor-pointer"
              >
                Limpar
              </button>
              <button
                type="submit"
                disabled={salvando}
                className="px-6 py-2 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold shadow-xs transition-colors cursor-pointer disabled:opacity-50"
              >
                {salvando ? 'Salvando...' : idPena ? 'Salvar Alterações' : 'Cadastrar Pena'}
              </button>
            </div>
          </form>
        </div>

        {/* Existing Penas List (5 cols) */}
        <div className="lg:col-span-5 bg-white rounded-2xl p-6 border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-sm">Penas Registradas</h3>
            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-rose-50 text-rose-700">
              {penas.length}
            </span>
          </div>

          <div className="space-y-3 max-h-[550px] overflow-y-auto pr-1">
            {penas.map((p) => {
              const u = usuarios.find(usr => usr.id_usuario === p.fk_usuario_id_usuario);
              const i = instituicoes.find(inst => inst.id_instituicao === p.fk_instituicao_id_instituicao);

              return (
                <div
                  key={p.id_pena}
                  className={`p-4 rounded-xl border transition-all ${
                    idPena === p.id_pena
                      ? 'border-rose-500 bg-rose-50/40'
                      : 'border-slate-200 hover:border-slate-300 bg-white'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <h4 className="text-xs font-bold text-slate-900">{u?.nome || `Apenado #${p.fk_usuario_id_usuario}`}</h4>
                      <p className="text-2xs text-rose-700 font-semibold mt-0.5">{p.tipo_pena}</p>
                    </div>
                    <div className="flex items-center space-x-1">
                      <button
                        onClick={() => handleEditar(p)}
                        title="Editar"
                        className="p-1 text-slate-600 hover:text-rose-600 hover:bg-slate-100 rounded-md cursor-pointer"
                      >
                        <Edit className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => handleExcluir(p.id_pena)}
                        title="Excluir"
                        className="p-1 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-md cursor-pointer"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>

                  <div className="mt-2 text-2xs text-slate-600 space-y-0.5">
                    <p>Instituição: <strong className="text-slate-800">{i?.nome || 'Não localizada'}</strong></p>
                    <p>Carga Horária: <strong className="text-slate-800">{p.horas_totais}h totais</strong> ({p.horas_semanais}h/sem)</p>
                    <p>Período: {formatarData(p.data_inicio)} até {formatarData(p.data_termino)}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
