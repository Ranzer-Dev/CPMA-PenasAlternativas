import React, { useState, useEffect, useRef } from 'react';
import { Usuario } from '../types';
import { formatarCPF, formatarTelefone, formatarCEP, validarCPF, limparCPF } from '../utils/formatters';
import {
  Users,
  Plus,
  Edit,
  Trash2,
  Camera,
  Upload,
  Check,
  X,
  AlertCircle,
  Search,
} from 'lucide-react';

interface CadastroUsuarioViewProps {
  usuarioParaEditar?: Usuario | null;
  onVoltar: () => void;
  onSalvo: () => void;
}

export const CadastroUsuarioView: React.FC<CadastroUsuarioViewProps> = ({
  usuarioParaEditar,
  onVoltar,
  onSalvo,
}) => {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [filtro, setFiltro] = useState('');

  // Form State
  const [idUsuario, setIdUsuario] = useState<number | undefined>(usuarioParaEditar?.id_usuario);
  const [codigo, setCodigo] = useState(usuarioParaEditar?.codigo || 'PENA-001');
  const [nome, setNome] = useState(usuarioParaEditar?.nome || '');
  const [cpf, setCpf] = useState(usuarioParaEditar?.cpf ? formatarCPF(usuarioParaEditar.cpf) : '');
  const [dataNascimento, setDataNascimento] = useState(usuarioParaEditar?.data_nascimento || '');
  const [telefone, setTelefone] = useState(usuarioParaEditar?.telefone ? formatarTelefone(usuarioParaEditar.telefone) : '');
  const [endereco, setEndereco] = useState(usuarioParaEditar?.endereco || '');
  const [bairro, setBairro] = useState(usuarioParaEditar?.bairro || '');
  const [cidade, setCidade] = useState(usuarioParaEditar?.cidade || 'Votorantim');
  const [uf, setUf] = useState(usuarioParaEditar?.uf || 'SP');
  const [cep, setCep] = useState(usuarioParaEditar?.cep ? formatarCEP(usuarioParaEditar.cep) : '');
  const [nacionalidade, setNacionalidade] = useState(usuarioParaEditar?.nacionalidade || 'Brasileira');
  const [observacao, setObservacao] = useState(usuarioParaEditar?.observacao || '');
  const [foto, setFoto] = useState(usuarioParaEditar?.foto || '');

  // Webcam Capture State
  const [cameraAtiva, setCameraAtiva] = useState(false);
  const videoRef = useRef<HTMLVideoElement | null>(null);

  const [erro, setErro] = useState<string | null>(null);
  const [sucesso, setSucesso] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  const carregarUsuarios = async () => {
    try {
      const res = await fetch('/api/v1/usuarios');
      if (res.ok) {
        const data = await res.json();
        setUsuarios(data);
      }
    } catch (e) {
      console.warn('Erro ao carregar lista de apenados:', e);
    }
  };

  useEffect(() => {
    carregarUsuarios();
  }, []);

  useEffect(() => {
    if (usuarioParaEditar) {
      setIdUsuario(usuarioParaEditar.id_usuario);
      setCodigo(usuarioParaEditar.codigo);
      setNome(usuarioParaEditar.nome);
      setCpf(formatarCPF(usuarioParaEditar.cpf));
      setDataNascimento(usuarioParaEditar.data_nascimento || '');
      setTelefone(formatarTelefone(usuarioParaEditar.telefone || ''));
      setEndereco(usuarioParaEditar.endereco || '');
      setBairro(usuarioParaEditar.bairro || '');
      setCidade(usuarioParaEditar.cidade || 'Votorantim');
      setUf(usuarioParaEditar.uf || 'SP');
      setCep(formatarCEP(usuarioParaEditar.cep || ''));
      setNacionalidade(usuarioParaEditar.nacionalidade || 'Brasileira');
      setObservacao(usuarioParaEditar.observacao || '');
      setFoto(usuarioParaEditar.foto || '');
    }
  }, [usuarioParaEditar]);

  const limparFormulario = () => {
    setIdUsuario(undefined);
    setCodigo(`PENA-${String(usuarios.length + 1).padStart(3, '0')}`);
    setNome('');
    setCpf('');
    setDataNascimento('');
    setTelefone('');
    setEndereco('');
    setBairro('');
    setCidade('Votorantim');
    setUf('SP');
    setCep('');
    setNacionalidade('Brasileira');
    setObservacao('');
    setFoto('');
    setErro(null);
    setSucesso(null);
  };

  const iniciarCamera = async () => {
    setCameraAtiva(true);
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: { width: 400, height: 400 } });
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        videoRef.current.play();
      }
    } catch (err) {
      console.warn('Câmera indisponível:', err);
      setErro('Não foi possível acessar a câmera do dispositivo.');
      setCameraAtiva(false);
    }
  };

  const capturarFoto = () => {
    if (videoRef.current) {
      const canvas = document.createElement('canvas');
      canvas.width = 300;
      canvas.height = 300;
      const ctx = canvas.getContext('2d');
      if (ctx) {
        ctx.drawImage(videoRef.current, 0, 0, 300, 300);
        const dataUrl = canvas.toDataURL('image/jpeg', 0.85);
        setFoto(dataUrl);
      }
      pararCamera();
    }
  };

  const pararCamera = () => {
    if (videoRef.current && videoRef.current.srcObject) {
      const stream = videoRef.current.srcObject as MediaStream;
      stream.getTracks().forEach(track => track.stop());
    }
    setCameraAtiva(false);
  };

  const handleUploadFoto = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setFoto(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro(null);
    setSucesso(null);

    const cpfLimpo = limparCPF(cpf);
    if (!nome.trim()) {
      setErro('O campo Nome é obrigatório.');
      return;
    }
    if (!cpfLimpo || cpfLimpo.length !== 11) {
      setErro('Informe um CPF válido com 11 dígitos.');
      return;
    }
    if (!validarCPF(cpfLimpo)) {
      setErro('O CPF informado possui dígitos verificadores inválidos.');
      return;
    }

    setSalvando(true);
    try {
      const url = idUsuario ? `/api/v1/usuarios/${idUsuario}` : '/api/v1/usuarios';
      const method = idUsuario ? 'PUT' : 'POST';

      const payload = {
        codigo,
        nome: nome.trim(),
        cpf: cpfLimpo,
        data_nascimento: dataNascimento,
        telefone: telefone.replace(/\D/g, ''),
        endereco,
        bairro,
        cidade,
        uf,
        cep: cep.replace(/\D/g, ''),
        nacionalidade,
        observacao,
        foto,
      };

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.error || 'Erro ao salvar apenado.');
      }

      setSucesso(idUsuario ? 'Apenado atualizado com sucesso!' : 'Apenado cadastrado com sucesso!');
      await carregarUsuarios();
      if (!idUsuario) {
        limparFormulario();
      }
      onSalvo();
    } catch (err: any) {
      setErro(err.message || 'Falha ao salvar dados.');
    } finally {
      setSalvando(false);
    }
  };

  const handleExcluir = async (id: number) => {
    if (!confirm('Deseja realmente excluir este apenado do sistema?')) return;
    try {
      const res = await fetch(`/api/v1/usuarios/${id}`, { method: 'DELETE' });
      if (res.ok) {
        await carregarUsuarios();
        if (idUsuario === id) limparFormulario();
      }
    } catch (err) {
      console.error('Erro ao excluir:', err);
    }
  };

  const usuariosFiltrados = usuarios.filter(u =>
    u.nome.toLowerCase().includes(filtro.toLowerCase()) ||
    u.cpf.includes(filtro.replace(/\D/g, '')) ||
    (u.codigo && u.codigo.toLowerCase().includes(filtro.toLowerCase()))
  );

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-8">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 flex items-center space-x-2">
            <Users className="w-6 h-6 text-blue-600" />
            <span>{idUsuario ? 'Editar Cadastro do Apenado' : 'Cadastro de Apenado'}</span>
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Gestão cadastral, biometria facial e controle de execução penal
          </p>
        </div>
        <button
          onClick={onVoltar}
          className="px-4 py-2 border border-slate-300 rounded-lg text-xs font-semibold text-slate-700 hover:bg-slate-50 cursor-pointer self-start sm:self-auto"
        >
          Voltar
        </button>
      </div>

      {/* Main Grid: Form + List */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left: Form (7 cols) */}
        <div className="lg:col-span-7 bg-white rounded-2xl p-6 sm:p-8 border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between pb-4 border-b border-slate-100 mb-6">
            <h3 className="font-bold text-slate-900 text-sm">Dados Pessoais e Cadastrais</h3>
            {idUsuario && (
              <button
                onClick={limparFormulario}
                className="text-xs font-semibold text-blue-600 hover:underline cursor-pointer"
              >
                + Novo Cadastro
              </button>
            )}
          </div>

          {erro && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg flex items-center space-x-2">
              <AlertCircle className="w-4 h-4 shrink-0 text-red-600" />
              <span>{erro}</span>
            </div>
          )}

          {sucesso && (
            <div className="mb-4 p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-lg flex items-center space-x-2">
              <Check className="w-4 h-4 shrink-0 text-emerald-600" />
              <span>{sucesso}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Foto e Código */}
            <div className="flex flex-col sm:flex-row items-center gap-6 p-4 bg-slate-50 rounded-xl border border-slate-200">
              <div className="relative w-28 h-28 rounded-2xl bg-white border-2 border-slate-200 overflow-hidden shrink-0 flex items-center justify-center shadow-xs">
                {cameraAtiva ? (
                  <video ref={videoRef} className="w-full h-full object-cover" autoPlay playsInline />
                ) : foto ? (
                  <img src={foto} alt="Foto do Apenado" className="w-full h-full object-cover" />
                ) : (
                  <Users className="w-12 h-12 text-slate-300" />
                )}
              </div>

              <div className="space-y-2 flex-1 w-full text-center sm:text-left">
                <span className="text-xs font-bold text-slate-700 uppercase block">Fotografia / Biometria</span>
                <div className="flex flex-wrap gap-2 justify-center sm:justify-start">
                  {cameraAtiva ? (
                    <>
                      <button
                        type="button"
                        onClick={capturarFoto}
                        className="px-3 py-1.5 bg-emerald-600 text-white rounded-lg text-xs font-bold hover:bg-emerald-700 cursor-pointer"
                      >
                        Capturar
                      </button>
                      <button
                        type="button"
                        onClick={pararCamera}
                        className="px-3 py-1.5 bg-slate-200 text-slate-700 rounded-lg text-xs font-bold hover:bg-slate-300 cursor-pointer"
                      >
                        Cancelar
                      </button>
                    </>
                  ) : (
                    <>
                      <button
                        type="button"
                        onClick={iniciarCamera}
                        className="flex items-center space-x-1 px-3 py-1.5 bg-blue-600 text-white rounded-lg text-xs font-bold hover:bg-blue-700 cursor-pointer"
                      >
                        <Camera className="w-3.5 h-3.5" />
                        <span>Webcam</span>
                      </button>
                      <label className="flex items-center space-x-1 px-3 py-1.5 bg-white border border-slate-300 text-slate-700 rounded-lg text-xs font-bold hover:bg-slate-50 cursor-pointer">
                        <Upload className="w-3.5 h-3.5" />
                        <span>Carregar</span>
                        <input type="file" accept="image/*" onChange={handleUploadFoto} className="hidden" />
                      </label>
                      {foto && (
                        <button
                          type="button"
                          onClick={() => setFoto('')}
                          className="px-2 py-1.5 text-xs text-red-600 hover:underline cursor-pointer"
                        >
                          Remover
                        </button>
                      )}
                    </>
                  )}
                </div>
              </div>

              <div className="w-full sm:w-auto">
                <label className="text-2xs font-bold text-slate-500 uppercase block">Código</label>
                <input
                  type="text"
                  value={codigo}
                  onChange={(e) => setCodigo(e.target.value)}
                  className="px-3 py-1.5 border border-slate-300 rounded-lg text-xs font-mono font-bold bg-white text-blue-700 w-full sm:w-28 text-center"
                />
              </div>
            </div>

            {/* Nome Completo */}
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                Nome Completo *
              </label>
              <input
                type="text"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                placeholder="Ex: João Pedro de Almeida"
                className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                required
              />
            </div>

            {/* CPF, Data Nasc, Telefone */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  CPF *
                </label>
                <input
                  type="text"
                  value={cpf}
                  onChange={(e) => setCpf(formatarCPF(e.target.value))}
                  placeholder="000.000.000-00"
                  maxLength={14}
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm font-mono focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Data de Nascimento
                </label>
                <input
                  type="date"
                  value={dataNascimento}
                  onChange={(e) => setDataNascimento(e.target.value)}
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Telefone
                </label>
                <input
                  type="text"
                  value={telefone}
                  onChange={(e) => setTelefone(formatarTelefone(e.target.value))}
                  placeholder="(15) 90000-0000"
                  maxLength={15}
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                />
              </div>
            </div>

            {/* Endereço e Bairro */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="sm:col-span-2">
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Endereço Residencial
                </label>
                <input
                  type="text"
                  value={endereco}
                  onChange={(e) => setEndereco(e.target.value)}
                  placeholder="Rua, número, complemento"
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Bairro
                </label>
                <input
                  type="text"
                  value={bairro}
                  onChange={(e) => setBairro(e.target.value)}
                  placeholder="Bairro"
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                />
              </div>
            </div>

            {/* Cidade, UF, CEP */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  Cidade
                </label>
                <input
                  type="text"
                  value={cidade}
                  onChange={(e) => setCidade(e.target.value)}
                  placeholder="Votorantim"
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                  UF
                </label>
                <input
                  type="text"
                  value={uf}
                  maxLength={2}
                  onChange={(e) => setUf(e.target.value.toUpperCase())}
                  placeholder="SP"
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm uppercase text-center focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                />
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
                  className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                />
              </div>
            </div>

            {/* Observações */}
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                Observações
              </label>
              <textarea
                value={observacao}
                onChange={(e) => setObservacao(e.target.value)}
                rows={2}
                placeholder="Observações do processo penal, restrições ou aptidões profissionais..."
                className="w-full px-3.5 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
              />
            </div>

            {/* Submit buttons */}
            <div className="flex items-center justify-end space-x-3 pt-2">
              <button
                type="button"
                onClick={limparFormulario}
                className="px-4 py-2.5 border border-slate-300 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-50 cursor-pointer"
              >
                Limpar
              </button>
              <button
                type="submit"
                disabled={salvando}
                className="px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold shadow-xs transition-colors cursor-pointer disabled:opacity-50"
              >
                {salvando ? 'Salvando...' : idUsuario ? 'Salvar Alterações' : 'Cadastrar Apenado'}
              </button>
            </div>
          </form>
        </div>

        {/* Right: Registered Apenados List (5 cols) */}
        <div className="lg:col-span-5 bg-white rounded-2xl p-6 border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <h3 className="font-bold text-slate-900 text-sm">Apenados Cadastrados</h3>
            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-50 text-blue-700">
              {usuarios.length}
            </span>
          </div>

          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={filtro}
              onChange={(e) => setFiltro(e.target.value)}
              placeholder="Filtrar por nome ou CPF..."
              className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-hidden focus:bg-white focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="space-y-2 max-h-[500px] overflow-y-auto pr-1">
            {usuariosFiltrados.map((u) => (
              <div
                key={u.id_usuario}
                className={`p-3 rounded-xl border transition-all flex items-center justify-between ${
                  idUsuario === u.id_usuario
                    ? 'border-blue-500 bg-blue-50/50'
                    : 'border-slate-200 hover:border-slate-300 bg-white'
                }`}
              >
                <div className="flex items-center space-x-3 min-w-0">
                  <div className="w-9 h-9 rounded-full bg-slate-200 shrink-0 overflow-hidden">
                    {u.foto ? (
                      <img src={u.foto} alt={u.nome} className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-slate-600 text-xs font-bold">
                        {u.nome.charAt(0)}
                      </div>
                    )}
                  </div>
                  <div className="min-w-0">
                    <p className="text-xs font-bold text-slate-900 truncate">{u.nome}</p>
                    <p className="text-2xs text-slate-500 font-mono">
                      {formatarCPF(u.cpf)} • {u.codigo}
                    </p>
                  </div>
                </div>

                <div className="flex items-center space-x-1 shrink-0 ml-2">
                  <button
                    onClick={() => {
                      setIdUsuario(u.id_usuario);
                      setCodigo(u.codigo);
                      setNome(u.nome);
                      setCpf(formatarCPF(u.cpf));
                      setDataNascimento(u.data_nascimento || '');
                      setTelefone(formatarTelefone(u.telefone || ''));
                      setEndereco(u.endereco || '');
                      setBairro(u.bairro || '');
                      setCidade(u.cidade || 'Votorantim');
                      setUf(u.uf || 'SP');
                      setCep(formatarCEP(u.cep || ''));
                      setNacionalidade(u.nacionalidade || 'Brasileira');
                      setObservacao(u.observacao || '');
                      setFoto(u.foto || '');
                    }}
                    title="Editar"
                    className="p-1.5 text-slate-600 hover:text-blue-600 hover:bg-slate-100 rounded-lg cursor-pointer"
                  >
                    <Edit className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => handleExcluir(u.id_usuario)}
                    title="Excluir"
                    className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg cursor-pointer"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
