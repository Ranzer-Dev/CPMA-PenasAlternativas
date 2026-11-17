import React, { useState, useEffect } from 'react';
import {
  Instituicao,
  TipoInstituicao,
  DisponibilidadeDTO,
  CadastroInstituicaoPayload,
} from '../types';
import { api, ApiError } from '../services/api';
import { formatarTelefone, formatarCEP } from '../utils/formatters';
import {
  Building2,
  Edit,
  Trash2,
  Clock,
  Check,
  AlertCircle,
  Search,
} from 'lucide-react';

interface CadastroInstituicaoViewProps {
  onVoltar: () => void;
}

export const CadastroInstituicaoView: React.FC<CadastroInstituicaoViewProps> = ({
  onVoltar,
}) => {
  const [instituicoes, setInstituicoes] = useState<Instituicao[]>([]);
  const [tipos, setTipos] = useState<TipoInstituicao[]>([]);
  const [filtro, setFiltro] = useState('');

  // Form State
  const [idInst, setIdInst] = useState<number | undefined>();
  const [nome, setNome] = useState('');
  const [endereco, setEndereco] = useState('');
  const [bairro, setBairro] = useState('');
  const [cidade, setCidade] = useState('Votorantim');
  const [uf, setUf] = useState('SP');
  const [cep, setCep] = useState('');
  const [responsavel, setResponsavel] = useState('');
  const [telefone, setTelefone] = useState('');
  const [tipoId, setTipoId] = useState<number>(0);

  // Disponibilidades
  const [disponibilidades, setDisponibilidades] = useState<DisponibilidadeDTO[]>([]);
  const [diaSemana, setDiaSemana] = useState('Segunda');
  const [horaInicio1, setHoraInicio1] = useState('08:00');
  const [horaFim1, setHoraFim1] = useState('12:00');
  const [horaInicio2, setHoraInicio2] = useState('13:00');
  const [horaFim2, setHoraFim2] = useState('17:00');

  // Modal Novo Tipo
  const [modalTipoAberto, setModalTipoAberto] = useState(false);
  const [novoTipoNome, setNovoTipoNome] = useState('');

  const [erro, setErro] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [sucesso, setSucesso] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  const carregarDados = async () => {
    try {
      const [instData, tiposData] = await Promise.all([
        api.get<Instituicao[]>('/instituicoes'),
        api.get<TipoInstituicao[]>('/instituicoes/tipos'),
      ]);
      setInstituicoes(instData || []);
      setTipos(tiposData || []);
      if (tiposData && tiposData.length > 0 && !tipoId) {
        setTipoId(tiposData[0].idTipo);
      }
    } catch (e: any) {
      setErro(e?.message || 'Erro ao carregar instituições.');
    }
  };

  useEffect(() => {
    carregarDados();
  }, []);

  const limparFormulario = () => {
    setIdInst(undefined);
    setNome('');
    setEndereco('');
    setBairro('');
    setCidade('Votorantim');
    setUf('SP');
    setCep('');
    setResponsavel('');
    setTelefone('');
    setTipoId(tipos.length > 0 ? tipos[0].idTipo : 0);
    setDisponibilidades([]);
    setFieldErrors({});
    setErro(null);
    setSucesso(null);
  };

  const handleAdicionarDisponibilidade = () => {
    if (!diaSemana) return;
    const nova: DisponibilidadeDTO = {
      diaSemana,
      horaInicio1: horaInicio1 || undefined,
      horaFim1: horaFim1 || undefined,
      horaInicio2: horaInicio2 || undefined,
      horaFim2: horaFim2 || undefined,
    };
    setDisponibilidades((prev) => [
      ...prev.filter((d) => d.diaSemana !== diaSemana),
      nova,
    ]);
  };

  const handleRemoverDisponibilidade = (dia: string) => {
    setDisponibilidades((prev) => prev.filter((d) => d.diaSemana !== dia));
  };

  const handleSalvarTipo = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!novoTipoNome.trim()) return;
    setErro(null);
    try {
      const criado = await api.post<TipoInstituicao>('/instituicoes/tipos', {
        tipo: novoTipoNome.trim(),
      });
      setTipos((prev) => [...prev, criado]);
      setTipoId(criado.idTipo);
      setNovoTipoNome('');
      setModalTipoAberto(false);
    } catch (err: any) {
      setErro(err?.message || 'Erro ao cadastrar novo tipo de instituição.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro(null);
    setFieldErrors({});
    setSucesso(null);

    if (!nome.trim()) {
      setErro('O nome da instituição é obrigatório.');
      setFieldErrors({ nome: 'O nome da instituição é obrigatório.' });
      return;
    }

    if (!tipoId) {
      setErro('Selecione ou crie um tipo de instituição.');
      return;
    }

    setSalvando(true);
    try {
      const payload: CadastroInstituicaoPayload = {
        nome: nome.trim(),
        endereco: endereco.trim() || undefined,
        bairro: bairro.trim() || undefined,
        cidade: cidade.trim() || 'Votorantim',
        uf: (uf.trim() || 'SP').toUpperCase(),
        cep: cep.replace(/\D/g, '') || undefined,
        responsavel: responsavel.trim() || undefined,
        telefone: telefone.replace(/\D/g, '') || undefined,
        tipoId,
        disponibilidades,
      };

      if (idInst) {
        await api.put<Instituicao>(`/instituicoes/${idInst}`, payload);
      } else {
        await api.post<Instituicao>('/instituicoes', payload);
      }

      setSucesso(
        idInst
          ? 'Instituição atualizada com sucesso!'
          : 'Instituição cadastrada com sucesso!'
      );
      await carregarDados();
      if (!idInst) limparFormulario();
    } catch (err: any) {
      if (err instanceof ApiError && err.fieldErrors) {
        setFieldErrors(err.fieldErrors);
      }
      setErro(err?.message || 'Falha ao salvar instituição.');
    } finally {
      setSalvando(false);
    }
  };

  const handleEditar = (inst: Instituicao) => {
    setIdInst(inst.idInstituicao);
    setNome(inst.nome);
    setEndereco(inst.endereco || '');
    setBairro(inst.bairro || '');
    setCidade(inst.cidade || 'Votorantim');
    setUf(inst.uf || 'SP');
    setCep(formatarCEP(inst.cep || ''));
    setResponsavel(inst.responsavel || '');
    setTelefone(formatarTelefone(inst.telefone || ''));
    setTipoId(inst.tipoId || (tipos[0]?.idTipo ?? 0));
    setDisponibilidades(inst.disponibilidades || []);
    setFieldErrors({});
    setErro(null);
    setSucesso(null);
  };

  const handleExcluir = async (id: number) => {
    if (!confirm('Deseja excluir esta instituição parceira?')) return;
    setErro(null);
    setSucesso(null);
    try {
      await api.delete(`/instituicoes/${id}`);
      setSucesso('Instituição excluída com sucesso.');
      await carregarDados();
      if (idInst === id) limparFormulario();
    } catch (e: any) {
      setErro(e?.message || 'Não foi possível excluir esta instituição.');
    }
  };

  const instituicoesFiltradas = instituicoes.filter(
    (i) =>
      i.nome.toLowerCase().includes(filtro.toLowerCase()) ||
      (i.responsavel && i.responsavel.toLowerCase().includes(filtro.toLowerCase())) ||
      (i.bairro && i.bairro.toLowerCase().includes(filtro.toLowerCase()))
  );

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 flex items-center space-x-2">
            <Building2 className="w-6 h-6 text-emerald-600" />
            <span>{idInst ? 'Editar Instituição Parceira' : 'Cadastro de Instituição'}</span>
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Gestão de entidades conveniadas, categorias e grade de horários disponíveis
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
            <h3 className="font-bold text-slate-900 text-sm">Dados da Instituição</h3>
            {idInst && (
              <button
                onClick={limparFormulario}
                className="text-xs font-semibold text-emerald-600 hover:underline cursor-pointer"
              >
                + Nova Instituição
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
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                Nome da Instituição *
              </label>
              <input
                type="text"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                placeholder="Ex: E.M. Professora Dely de Oliveira"
                className={`w-full px-3.5 py-2.5 border rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-hidden ${
                  fieldErrors.nome ? 'border-red-500 bg-red-50/30' : 'border-slate-300'
                }`}
                required
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="sm:col-span-2">
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Tipo / Categoria *
                </label>
                <div className="flex space-x-2">
                  <select
                    value={tipoId}
                    onChange={(e) => setTipoId(Number(e.target.value))}
                    className="flex-1 px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-emerald-500 focus:outline-hidden"
                    required
                  >
                    <option value={0} disabled>
                      Selecione um tipo...
                    </option>
                    {tipos.map((t) => (
                      <option key={t.idTipo} value={t.idTipo}>
                        {t.tipo}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    onClick={() => setModalTipoAberto(true)}
                    className="px-2.5 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-xs font-bold border border-slate-300 cursor-pointer"
                  >
                    + Novo Tipo
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Telefone
                </label>
                <input
                  type="text"
                  value={telefone}
                  onChange={(e) => setTelefone(formatarTelefone(e.target.value))}
                  placeholder="(15) 3243-0000"
                  maxLength={15}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-hidden"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Responsável pelo Convênio
                </label>
                <input
                  type="text"
                  value={responsavel}
                  onChange={(e) => setResponsavel(e.target.value)}
                  placeholder="Nome do diretor ou coordenador"
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Endereço
                </label>
                <input
                  type="text"
                  value={endereco}
                  onChange={(e) => setEndereco(e.target.value)}
                  placeholder="Rua, número"
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-hidden"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Bairro
                </label>
                <input
                  type="text"
                  value={bairro}
                  onChange={(e) => setBairro(e.target.value)}
                  placeholder="Bairro"
                  className="w-full px-3.5 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Cidade / UF
                </label>
                <div className="flex gap-1.5">
                  <input
                    type="text"
                    value={cidade}
                    onChange={(e) => setCidade(e.target.value)}
                    placeholder="Votorantim"
                    className="flex-1 px-2.5 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-hidden"
                  />
                  <input
                    type="text"
                    value={uf}
                    maxLength={2}
                    onChange={(e) => setUf(e.target.value.toUpperCase())}
                    placeholder="SP"
                    className="w-12 px-2 py-2 border border-slate-300 rounded-lg text-xs text-center uppercase focus:ring-2 focus:ring-emerald-500 focus:outline-hidden"
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  CEP
                </label>
                <input
                  type="text"
                  value={cep}
                  onChange={(e) => setCep(formatarCEP(e.target.value))}
                  placeholder="18110-000"
                  maxLength={9}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-hidden"
                />
              </div>
            </div>

            {/* Disponibilidade de Horários */}
            <div className="pt-4 border-t border-slate-100 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-800 uppercase flex items-center space-x-1.5">
                  <Clock className="w-4 h-4 text-emerald-600" />
                  <span>Dias e Horários Disponíveis para Trabalho</span>
                </span>
              </div>

              <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 grid grid-cols-2 sm:grid-cols-6 gap-2 items-end">
                <div>
                  <label className="text-2xs font-semibold text-slate-600">Dia</label>
                  <select
                    value={diaSemana}
                    onChange={(e) => setDiaSemana(e.target.value)}
                    className="w-full px-2 py-1.5 border border-slate-300 rounded-lg text-xs bg-white"
                  >
                    <option value="Segunda">Segunda</option>
                    <option value="Terça">Terça</option>
                    <option value="Quarta">Quarta</option>
                    <option value="Quinta">Quinta</option>
                    <option value="Sexta">Sexta</option>
                    <option value="Sábado">Sábado</option>
                    <option value="Domingo">Domingo</option>
                  </select>
                </div>
                <div>
                  <label className="text-2xs font-semibold text-slate-600">T1 Início</label>
                  <input
                    type="time"
                    value={horaInicio1}
                    onChange={(e) => setHoraInicio1(e.target.value)}
                    className="w-full px-2 py-1.5 border border-slate-300 rounded-lg text-xs bg-white"
                  />
                </div>
                <div>
                  <label className="text-2xs font-semibold text-slate-600">T1 Fim</label>
                  <input
                    type="time"
                    value={horaFim1}
                    onChange={(e) => setHoraFim1(e.target.value)}
                    className="w-full px-2 py-1.5 border border-slate-300 rounded-lg text-xs bg-white"
                  />
                </div>
                <div>
                  <label className="text-2xs font-semibold text-slate-600">T2 Início</label>
                  <input
                    type="time"
                    value={horaInicio2}
                    onChange={(e) => setHoraInicio2(e.target.value)}
                    className="w-full px-2 py-1.5 border border-slate-300 rounded-lg text-xs bg-white"
                  />
                </div>
                <div>
                  <label className="text-2xs font-semibold text-slate-600">T2 Fim</label>
                  <input
                    type="time"
                    value={horaFim2}
                    onChange={(e) => setHoraFim2(e.target.value)}
                    className="w-full px-2 py-1.5 border border-slate-300 rounded-lg text-xs bg-white"
                  />
                </div>
                <button
                  type="button"
                  onClick={handleAdicionarDisponibilidade}
                  className="px-3 py-1.5 bg-emerald-600 text-white rounded-lg text-xs font-bold hover:bg-emerald-700 cursor-pointer h-[34px]"
                >
                  + Incluir
                </button>
              </div>

              {disponibilidades.length > 0 && (
                <div className="space-y-1.5">
                  {disponibilidades.map((d) => (
                    <div
                      key={d.diaSemana}
                      className="flex items-center justify-between px-3 py-2 bg-emerald-50/50 border border-emerald-200 rounded-lg text-xs"
                    >
                      <span className="font-bold text-slate-800">{d.diaSemana}:</span>
                      <span className="text-slate-600 font-mono">
                        {d.horaInicio1} às {d.horaFim1}
                        {d.horaInicio2 && ` e ${d.horaInicio2} às ${d.horaFim2}`}
                      </span>
                      <button
                        type="button"
                        onClick={() => handleRemoverDisponibilidade(d.diaSemana)}
                        className="text-red-600 hover:text-red-800 font-bold px-2 py-0.5 cursor-pointer"
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>

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
                className="px-6 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-xs transition-colors cursor-pointer disabled:opacity-50"
              >
                {salvando
                  ? 'Salvando...'
                  : idInst
                  ? 'Salvar Alterações'
                  : 'Cadastrar Instituição'}
              </button>
            </div>
          </form>
        </div>

        {/* List of Registered Institutions (5 cols) */}
        <div className="lg:col-span-5 bg-white rounded-2xl p-6 border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-sm">Instituições Cadastradas</h3>
            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700">
              {instituicoes.length}
            </span>
          </div>

          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={filtro}
              onChange={(e) => setFiltro(e.target.value)}
              placeholder="Filtrar instituições..."
              className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-hidden focus:bg-white focus:ring-2 focus:ring-emerald-500"
            />
          </div>

          <div className="space-y-3 max-h-[550px] overflow-y-auto pr-1">
            {instituicoesFiltradas.map((inst) => (
              <div
                key={inst.idInstituicao}
                className={`p-4 rounded-xl border transition-all ${
                  idInst === inst.idInstituicao
                    ? 'border-emerald-500 bg-emerald-50/40'
                    : 'border-slate-200 hover:border-slate-300 bg-white'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h4 className="text-xs font-bold text-slate-900">{inst.nome}</h4>
                    <span className="inline-block px-2 py-0.5 bg-slate-100 text-slate-600 rounded-md text-2xs font-semibold mt-1">
                      {inst.tipoNome || 'Convênio'}
                    </span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <button
                      onClick={() => handleEditar(inst)}
                      title="Editar"
                      className="p-1 text-slate-600 hover:text-emerald-600 hover:bg-slate-100 rounded-md cursor-pointer"
                    >
                      <Edit className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => handleExcluir(inst.idInstituicao)}
                      title="Excluir"
                      className="p-1 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-md cursor-pointer"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>

                <p className="text-2xs text-slate-500 mt-2">
                  {inst.endereco ? `${inst.endereco}, ` : ''}
                  {inst.bairro ? `${inst.bairro} • ` : ''}
                  {inst.cidade}
                </p>
                {inst.responsavel && (
                  <p className="text-2xs text-slate-500 mt-0.5">
                    Resp: <strong className="text-slate-700">{inst.responsavel}</strong>{' '}
                    {inst.telefone && `• ${formatarTelefone(inst.telefone)}`}
                  </p>
                )}

                {inst.disponibilidades && inst.disponibilidades.length > 0 && (
                  <div className="mt-2 pt-2 border-t border-slate-100 flex flex-wrap gap-1">
                    {inst.disponibilidades.map((d) => (
                      <span
                        key={d.diaSemana}
                        className="text-3xs px-1.5 py-0.5 bg-slate-100 text-slate-600 rounded"
                      >
                        {d.diaSemana}: {d.horaInicio1}-{d.horaFim1}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Modal Novo Tipo de Instituição */}
      {modalTipoAberto && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl p-6 max-w-sm w-full space-y-4 shadow-xl border border-slate-200">
            <h4 className="font-bold text-slate-900 text-sm">Novo Tipo de Instituição</h4>
            <form onSubmit={handleSalvarTipo} className="space-y-3">
              <input
                type="text"
                value={novoTipoNome}
                onChange={(e) => setNovoTipoNome(e.target.value)}
                placeholder="Ex: Associação de Bairro"
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-hidden"
                autoFocus
                required
              />
              <div className="flex justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setModalTipoAberto(false)}
                  className="px-3 py-1.5 border border-slate-300 rounded-lg text-xs font-semibold text-slate-600 cursor-pointer"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-bold cursor-pointer"
                >
                  Salvar Tipo
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
