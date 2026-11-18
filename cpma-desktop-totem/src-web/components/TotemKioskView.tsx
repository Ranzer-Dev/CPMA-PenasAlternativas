import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  Shield,
  KeyRound,
  Camera,
  CheckCircle2,
  AlertTriangle,
  Wifi,
  WifiOff,
  Delete,
  RotateCcw,
  ArrowRight,
  UserCheck,
  Clock,
  Building2,
  Calendar,
  FileText,
  Printer,
  LogOut,
  Settings,
  RefreshCw,
  Sparkles,
  Award,
} from 'lucide-react';
import {
  TotemConfig,
  TotemStatusResponse,
  ValidarCodigoEtapa1Response,
  ProntuarioApenado,
} from '../types';
import { totemApi } from '../services/totemApi';
import { facialService } from '../services/facialService';

interface TotemKioskViewProps {
  config: TotemConfig;
  serverStatus: TotemStatusResponse | null;
  onOpenConfig: () => void;
  onRefreshStatus: () => void;
}

type EtapaFluxo = 'ETAPA_1_PIN' | 'ETAPA_2_BIOMETRIA' | 'ETAPA_3_PRONTUARIO';

const TEMPO_SESSAO_SEGUNDOS = 45;

function mascararCpf(cpf?: string): string {
  if (!cpf) return '***.***.***-**';
  const limpo = cpf.replace(/\D/g, '');
  if (limpo.length !== 11) return cpf;
  return `***.${limpo.slice(3, 6)}.${limpo.slice(6, 9)}-**`;
}

function formatarDataBr(iso?: string): string {
  if (!iso) return '—';
  const partes = iso.split('T')[0].split('-');
  if (partes.length === 3) {
    return `${partes[2]}/${partes[1]}/${partes[0]}`;
  }
  return iso;
}

export const TotemKioskView: React.FC<TotemKioskViewProps> = ({
  config,
  serverStatus,
  onOpenConfig,
  onRefreshStatus,
}) => {
  const [etapa, setEtapa] = useState<EtapaFluxo>('ETAPA_1_PIN');
  const [pin, setPin] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [erro, setErro] = useState<string | null>(null);

  // Dados da Etapa 1 -> Etapa 2
  const [dadosEtapa1, setDadosEtapa1] = useState<ValidarCodigoEtapa1Response | null>(null);

  // Câmera na Etapa 2
  const [usarSidecarLocal, setUsarSidecarLocal] = useState<boolean>(false);
  const [streamMjpegUrl, setStreamMjpegUrl] = useState<string>('');
  const [camerasWebRtc, setCamerasWebRtc] = useState<MediaDeviceInfo[]>([]);
  const [selectedDeviceId, setSelectedDeviceId] = useState<string>('');
  const [cameraPronta, setCameraPronta] = useState<boolean>(false);
  const [similaridadeObtida, setSimilaridadeObtida] = useState<number | null>(null);

  const videoRef = useRef<HTMLVideoElement | null>(null);
  const mediaStreamRef = useRef<MediaStream | null>(null);

  // Dados da Etapa 3 (Prontuário)
  const [prontuario, setProntuario] = useState<ProntuarioApenado | null>(null);
  const [segundosRestantes, setSegundosRestantes] = useState<number>(TEMPO_SESSAO_SEGUNDOS);

  const pararWebcamWebRtc = useCallback(() => {
    if (mediaStreamRef.current) {
      mediaStreamRef.current.getTracks().forEach((track) => track.stop());
      mediaStreamRef.current = null;
    }
    setCameraPronta(false);
  }, []);

  const reiniciarFluxo = useCallback(() => {
    pararWebcamWebRtc();
    setEtapa('ETAPA_1_PIN');
    setPin('');
    setErro(null);
    setLoading(false);
    setDadosEtapa1(null);
    setProntuario(null);
    setSimilaridadeObtida(null);
    setSegundosRestantes(TEMPO_SESSAO_SEGUNDOS);
  }, [pararWebcamWebRtc]);

  // Suporte a teclado numérico físico na Etapa 1
  useEffect(() => {
    if (etapa !== 'ETAPA_1_PIN') return;

    const handleKeyDown = (ev: KeyboardEvent) => {
      if (loading) return;
      if (ev.ctrlKey || ev.altKey || ev.metaKey) return;

      // Não intercepta teclas quando o usuário está digitando em um input/modal (ex: ConfigServidorModal)
      const target = ev.target as HTMLElement | null;
      if (
        target &&
        (target.tagName === 'INPUT' ||
          target.tagName === 'TEXTAREA' ||
          target.tagName === 'SELECT' ||
          target.isContentEditable ||
          target.closest('[role="dialog"]'))
      ) {
        return;
      }

      if (/^[0-9]$/.test(ev.key)) {
        ev.preventDefault();
        setErro(null);
        setPin((prev) => (prev.length < 6 ? prev + ev.key : prev));
      } else if (ev.key === 'Backspace') {
        ev.preventDefault();
        setErro(null);
        setPin((prev) => prev.slice(0, -1));
      } else if (ev.key === 'Escape') {
        ev.preventDefault();
        setErro(null);
        setPin('');
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [etapa, loading]);

  // Inicializa câmera ao entrar na Etapa 2
  useEffect(() => {
    if (etapa !== 'ETAPA_2_BIOMETRIA') {
      pararWebcamWebRtc();
      return;
    }

    let cancelado = false;

    const iniciarCamera = async () => {
      setErro(null);
      setCameraPronta(false);

      // 1. Verifica se existe cpma-facial-service local na máquina do Totem (porta 8001)
      const sidecarAtivo = await facialService.checkLocalSidecarHealth();
      if (cancelado) return;

      if (sidecarAtivo) {
        setUsarSidecarLocal(true);
        setStreamMjpegUrl(facialService.getLocalMjpegStreamUrl(0));
        setCameraPronta(true);
        return;
      }

      // 2. Fallback automático para WebRTC getUserMedia (webcam conectada ao Totem)
      setUsarSidecarLocal(false);
      try {
        const constraints: MediaStreamConstraints = {
          video: selectedDeviceId
            ? { deviceId: { exact: selectedDeviceId }, width: { ideal: 640 }, height: { ideal: 480 } }
            : { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' },
          audio: false,
        };

        const stream = await navigator.mediaDevices.getUserMedia(constraints);
        if (cancelado) {
          stream.getTracks().forEach((t) => t.stop());
          return;
        }

        mediaStreamRef.current = stream;
        if (videoRef.current) {
          videoRef.current.srcObject = stream;
          await videoRef.current.play().catch(() => {});
        }
        setCameraPronta(true);

        const devices = await navigator.mediaDevices.enumerateDevices();
        const videoDevices = devices.filter((d) => d.kind === 'videoinput');
        setCamerasWebRtc(videoDevices);
      } catch (err: any) {
        setErro(
          'Não foi possível acessar a câmera deste terminal. Verifique se a webcam está conectada.',
        );
      }
    };

    iniciarCamera();

    return () => {
      cancelado = true;
      pararWebcamWebRtc();
    };
  }, [etapa, selectedDeviceId, pararWebcamWebRtc]);

  // Timer regressivo de privacidade (LGPD) na Etapa 3
  useEffect(() => {
    if (etapa !== 'ETAPA_3_PRONTUARIO') return;

    const timer = setInterval(() => {
      setSegundosRestantes((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          reiniciarFluxo();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [etapa, reiniciarFluxo]);

  // Submete Etapa 1 (PIN de 6 dígitos)
  const handleValidarPinEtapa1 = useCallback(
    async (codigoPin: string) => {
      if (codigoPin.length !== 6 || loading) return;
      setLoading(true);
      setErro(null);

      try {
        const resp = await totemApi.validarCodigoEtapa1(codigoPin);
        const valido = resp.sucesso ?? resp.codigoValido;
        if (!valido || resp.etapaSeguinte === 'BLOQUEADO') {
          setErro(resp.mensagem || 'Código inválido, expirado ou já utilizado.');
          setPin('');
          return;
        }
        setDadosEtapa1(resp);
        setEtapa('ETAPA_2_BIOMETRIA');
      } catch (err: any) {
        setErro(err?.message || 'Falha ao validar código no servidor da Delegacia.');
        setPin('');
      } finally {
        setLoading(false);
      }
    },
    [loading],
  );

  // Auto-dispara Etapa 1 quando os 6 dígitos são preenchidos
  useEffect(() => {
    if (etapa === 'ETAPA_1_PIN' && pin.length === 6 && !loading) {
      handleValidarPinEtapa1(pin);
    }
  }, [pin, etapa, loading, handleValidarPinEtapa1]);

  // Captura frame e valida Etapa 2 (Biometria Facial) + carrega Prontuário
  const handleCapturarEValidarBiometria = async () => {
    if (loading) return;
    setLoading(true);
    setErro(null);

    try {
      let fotoBase64 = '';

      if (usarSidecarLocal) {
        fotoBase64 = await facialService.captureFromLocalSidecar(0);
      } else {
        const video = videoRef.current;
        if (!video || video.readyState < 2) {
          throw new Error('A câmera ainda não está pronta para captura.');
        }
        const canvas = document.createElement('canvas');
        canvas.width = video.videoWidth || 640;
        canvas.height = video.videoHeight || 480;
        const ctx = canvas.getContext('2d');
        if (!ctx) throw new Error('Erro ao inicializar captura gráfica.');
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        const dataUrl = canvas.toDataURL('image/jpeg', 0.9);
        fotoBase64 = dataUrl.replace(/^data:image\/\w+;base64,/, '');
      }

      const resultadoBio = await totemApi.validarBiometriaEtapa2(pin, fotoBase64, 0.6);
      const bioAutorizada = resultadoBio.sucesso ?? resultadoBio.autorizado;

      if (!bioAutorizada || !resultadoBio.tokenSessao || !resultadoBio.usuarioId) {
        setErro(
          resultadoBio.mensagem ||
            resultadoBio.motivo ||
            'Biometria facial não reconhecida. Posicione o rosto de frente para a câmera e tente novamente.',
        );
        return;
      }

      setSimilaridadeObtida(resultadoBio.similaridadeObtida ?? null);

      // Carrega o prontuário completo do apenado usando o JWT APENADO_SESSION
      const dadosProntuario = await totemApi.carregarProntuarioApenado(
        resultadoBio.usuarioId,
        resultadoBio.tokenSessao,
      );

      pararWebcamWebRtc();
      setProntuario(dadosProntuario);
      setSegundosRestantes(TEMPO_SESSAO_SEGUNDOS);
      setEtapa('ETAPA_3_PRONTUARIO');
    } catch (err: any) {
      setErro(
        err?.message ||
          'Não foi possível concluir a validação biométrica. Tente novamente ou procure o agente.',
      );
    } finally {
      setLoading(false);
    }
  };

  const handleImprimirComprovante = () => {
    if (window.electronAPI?.print) {
      window.electronAPI.print();
    } else {
      window.print();
    }
  };

  const adicionarDigito = (digito: string) => {
    if (loading) return;
    setErro(null);
    setPin((prev) => (prev.length < 6 ? prev + digito : prev));
  };

  const apagarDigito = () => {
    if (loading) return;
    setErro(null);
    setPin((prev) => prev.slice(0, -1));
  };

  const limparPin = () => {
    if (loading) return;
    setErro(null);
    setPin('');
  };

  const online =
    !!serverStatus && (serverStatus.status === 'UP' || serverStatus.status === 'ONLINE');

  return (
    <div className="min-h-screen flex flex-col bg-gradient-to-b from-slate-950 via-slate-900 to-slate-950 text-slate-100">
      {/* BARRA SUPERIOR INSTITUCIONAL (OCULTA NA IMPRESSÃO) */}
      <header className="no-print border-b border-slate-800/80 bg-slate-950/90 backdrop-blur px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-600/20 border border-blue-500/40 text-blue-400 shadow-inner">
            <Shield className="h-7 w-7" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold uppercase tracking-widest text-blue-400">
                Governo do Estado de São Paulo • SAP / CPMA
              </span>
            </div>
            <h1 className="text-xl font-extrabold tracking-tight text-white">
              Terminal de Autoatendimento e Consulta do Apenado
            </h1>
          </div>
        </div>

        {/* Status de Conexão LAN com o PC do Delegado */}
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onRefreshStatus}
            title="Atualizar status da conexão LAN"
            className="rounded-xl border border-slate-800 bg-slate-900 p-2.5 text-slate-400 hover:bg-slate-800 hover:text-white transition"
          >
            <RefreshCw className="h-4 w-4" />
          </button>

          <button
            type="button"
            onClick={onOpenConfig}
            className={`flex items-center gap-2.5 rounded-xl border px-4 py-2 text-xs font-semibold transition ${
              online
                ? 'border-emerald-500/40 bg-emerald-950/40 text-emerald-300 hover:bg-emerald-900/50'
                : 'border-rose-500/50 bg-rose-950/50 text-rose-300 hover:bg-rose-900/60 animate-pulse'
            }`}
          >
            {online ? (
              <Wifi className="h-4 w-4 text-emerald-400" />
            ) : (
              <WifiOff className="h-4 w-4 text-rose-400" />
            )}
            <div className="text-left">
              <div className="font-bold">
                {online ? 'Conectado ao PC do Delegado' : 'Sem Conexão com Servidor'}
              </div>
              <div className="text-[11px] opacity-80 font-mono">
                LAN: {config.serverIp}:{config.serverPort}
              </div>
            </div>
            <Settings className="h-4 w-4 ml-1 opacity-75" />
          </button>
        </div>
      </header>

      {/* INDICADOR DE ETAPAS (STEPPER) */}
      <div className="no-print border-b border-slate-800/60 bg-slate-900/40 px-6 py-3">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <div
            className={`flex items-center gap-2.5 text-sm font-bold ${
              etapa === 'ETAPA_1_PIN' ? 'text-blue-400' : 'text-emerald-400'
            }`}
          >
            <span
              className={`flex h-7 w-7 items-center justify-center rounded-full text-xs font-extrabold ${
                etapa === 'ETAPA_1_PIN'
                  ? 'bg-blue-600 text-white ring-4 ring-blue-500/20'
                  : 'bg-emerald-600 text-white'
              }`}
            >
              1
            </span>
            <span>Código de Acesso (PIN)</span>
          </div>

          <div className="h-0.5 flex-1 mx-4 bg-slate-800" />

          <div
            className={`flex items-center gap-2.5 text-sm font-bold ${
              etapa === 'ETAPA_2_BIOMETRIA'
                ? 'text-blue-400'
                : etapa === 'ETAPA_3_PRONTUARIO'
                ? 'text-emerald-400'
                : 'text-slate-500'
            }`}
          >
            <span
              className={`flex h-7 w-7 items-center justify-center rounded-full text-xs font-extrabold ${
                etapa === 'ETAPA_2_BIOMETRIA'
                  ? 'bg-blue-600 text-white ring-4 ring-blue-500/20'
                  : etapa === 'ETAPA_3_PRONTUARIO'
                  ? 'bg-emerald-600 text-white'
                  : 'bg-slate-800 text-slate-400'
              }`}
            >
              2
            </span>
            <span>Reconhecimento Facial</span>
          </div>

          <div className="h-0.5 flex-1 mx-4 bg-slate-800" />

          <div
            className={`flex items-center gap-2.5 text-sm font-bold ${
              etapa === 'ETAPA_3_PRONTUARIO' ? 'text-emerald-400' : 'text-slate-500'
            }`}
          >
            <span
              className={`flex h-7 w-7 items-center justify-center rounded-full text-xs font-extrabold ${
                etapa === 'ETAPA_3_PRONTUARIO'
                  ? 'bg-emerald-600 text-white ring-4 ring-emerald-500/20'
                  : 'bg-slate-800 text-slate-400'
              }`}
            >
              3
            </span>
            <span>Prontuário e Horas Cumpridas</span>
          </div>
        </div>
      </div>

      {/* CONTEÚDO PRINCIPAL */}
      <main className="flex-1 flex flex-col justify-center p-6 max-w-6xl w-full mx-auto">
        {/* ============================================================
            ETAPA 1: DIGITAÇÃO DO PIN DE 6 DÍGITOS
           ============================================================ */}
        {etapa === 'ETAPA_1_PIN' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
            {/* Coluna Esquerda: Instruções e Slots do PIN */}
            <div className="lg:col-span-7 space-y-6">
              <div className="space-y-2">
                <span className="inline-flex items-center gap-2 rounded-full border border-blue-500/30 bg-blue-500/10 px-3.5 py-1 text-xs font-bold uppercase tracking-wider text-blue-300">
                  <KeyRound className="h-3.5 w-3.5" /> Etapa 1 de 3 • Identificação por Código
                </span>
                <h2 className="text-3xl font-extrabold text-white leading-tight">
                  Digite seu Código de Acesso de 6 dígitos
                </h2>
                <p className="text-slate-300 text-base">
                  Insira o código temporário fornecido pelo agente na recepção da Delegacia para
                  iniciar sua consulta de horas cumpridas.
                </p>
              </div>

              {/* Visor de 6 dígitos */}
              <div className="grid grid-cols-6 gap-3 max-w-lg">
                {[0, 1, 2, 3, 4, 5].map((index) => {
                  const char = pin[index] || '';
                  const isCurrent = index === pin.length;
                  return (
                    <div
                      key={index}
                      className={`h-20 rounded-2xl border-2 flex items-center justify-center text-4xl font-mono font-extrabold transition-all ${
                        char
                          ? 'border-blue-500 bg-blue-950/40 text-white shadow-lg shadow-blue-900/20'
                          : isCurrent
                          ? 'border-blue-400/70 bg-slate-900/90 text-slate-400 ring-4 ring-blue-500/15'
                          : 'border-slate-800 bg-slate-900/50 text-slate-600'
                      }`}
                    >
                      {char || '•'}
                    </div>
                  );
                })}
              </div>

              {erro && (
                <div className="rounded-2xl border border-rose-500/40 bg-rose-950/40 p-4 text-rose-200 flex items-start gap-3">
                  <AlertTriangle className="h-6 w-6 text-rose-400 shrink-0 mt-0.5" />
                  <div>
                    <p className="font-bold text-sm">Acesso não autorizado</p>
                    <p className="text-sm opacity-90">{erro}</p>
                  </div>
                </div>
              )}

              {!online && (
                <div className="rounded-2xl border border-amber-500/40 bg-amber-950/30 p-4 text-amber-200 flex items-center justify-between gap-4">
                  <div className="flex items-start gap-3">
                    <WifiOff className="h-6 w-6 text-amber-400 shrink-0 mt-0.5" />
                    <div>
                      <p className="font-bold text-sm">
                        Servidor da Delegacia indisponível ({config.serverIp}:{config.serverPort})
                      </p>
                      <p className="text-xs opacity-90">
                        Verifique se o cabo de rede está conectado e se o sistema no PC do Delegado
                        está aberto.
                      </p>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={onOpenConfig}
                    className="shrink-0 rounded-xl bg-amber-500/20 border border-amber-400/40 px-3.5 py-2 text-xs font-bold text-amber-200 hover:bg-amber-500/30"
                  >
                    Configurar IP
                  </button>
                </div>
              )}

              <div className="rounded-2xl border border-slate-800/90 bg-slate-900/50 p-4 text-xs text-slate-400 space-y-1">
                <p className="font-semibold text-slate-200">Como funciona sua consulta:</p>
                <p>1. Digite o código de 6 dígitos emitido pelo Delegado ou Agente.</p>
                <p>2. Olhe para a câmera do Totem para confirmar sua identidade facial.</p>
                <p>3. Consulte ou imprima seu extrato atualizado de cumprimento de pena.</p>
              </div>
            </div>

            {/* Coluna Direita: Teclado Numérico Touch (Kiosk) */}
            <div className="lg:col-span-5">
              <div className="rounded-3xl border border-slate-800 bg-slate-900/80 p-6 shadow-2xl space-y-4">
                <div className="grid grid-cols-3 gap-3">
                  {['1', '2', '3', '4', '5', '6', '7', '8', '9'].map((num) => (
                    <button
                      key={num}
                      type="button"
                      disabled={loading || pin.length >= 6}
                      onClick={() => adicionarDigito(num)}
                      className="h-18 rounded-2xl border border-slate-700/80 bg-slate-800/90 hover:bg-slate-700 active:scale-95 text-2xl font-extrabold text-white shadow transition disabled:opacity-50"
                    >
                      {num}
                    </button>
                  ))}

                  <button
                    type="button"
                    disabled={loading || pin.length === 0}
                    onClick={limparPin}
                    className="h-18 rounded-2xl border border-rose-800/60 bg-rose-950/40 hover:bg-rose-900/50 active:scale-95 flex flex-col items-center justify-center text-xs font-bold text-rose-300 transition disabled:opacity-40"
                  >
                    <RotateCcw className="h-5 w-5 mb-0.5" />
                    Limpar
                  </button>

                  <button
                    type="button"
                    disabled={loading || pin.length >= 6}
                    onClick={() => adicionarDigito('0')}
                    className="h-18 rounded-2xl border border-slate-700/80 bg-slate-800/90 hover:bg-slate-700 active:scale-95 text-2xl font-extrabold text-white shadow transition disabled:opacity-50"
                  >
                    0
                  </button>

                  <button
                    type="button"
                    disabled={loading || pin.length === 0}
                    onClick={apagarDigito}
                    className="h-18 rounded-2xl border border-slate-700 bg-slate-800/60 hover:bg-slate-700 active:scale-95 flex flex-col items-center justify-center text-xs font-bold text-slate-300 transition disabled:opacity-40"
                  >
                    <Delete className="h-5 w-5 mb-0.5" />
                    Apagar
                  </button>
                </div>

                <button
                  type="button"
                  disabled={loading || pin.length !== 6}
                  onClick={() => handleValidarPinEtapa1(pin)}
                  className="w-full h-16 rounded-2xl bg-blue-600 hover:bg-blue-500 active:scale-[0.99] disabled:bg-slate-800 disabled:text-slate-500 text-lg font-extrabold text-white shadow-lg shadow-blue-900/40 flex items-center justify-center gap-3 transition"
                >
                  {loading ? (
                    <>
                      <RefreshCw className="h-5 w-5 animate-spin" />
                      Validando Código...
                    </>
                  ) : (
                    <>
                      Continuar para Biometria Facial
                      <ArrowRight className="h-5 w-5" />
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* ============================================================
            ETAPA 2: VALIDAÇÃO BIOMÉTRICA FACIAL
           ============================================================ */}
        {etapa === 'ETAPA_2_BIOMETRIA' && dadosEtapa1 && (
          <div className="max-w-4xl mx-auto w-full space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-blue-500/30 bg-blue-950/30 px-6 py-4">
              <div className="flex items-center gap-4">
                <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-600 text-white">
                  <UserCheck className="h-6 w-6" />
                </div>
                <div>
                  <span className="text-xs font-bold uppercase tracking-wider text-blue-300">
                    Código Validado • Etapa 2 de 3
                  </span>
                  <h2 className="text-2xl font-extrabold text-white">
                    Olá, {dadosEtapa1.usuarioNome || dadosEtapa1.nomeApenado || 'Prestador'}
                  </h2>
                </div>
              </div>

              <button
                type="button"
                disabled={loading}
                onClick={reiniciarFluxo}
                className="rounded-xl border border-slate-700 bg-slate-800/80 px-4 py-2.5 text-xs font-bold text-slate-300 hover:bg-slate-700 transition"
              >
                Cancelar / Voltar ao Início
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-center">
              {/* Visor da Câmera */}
              <div className="md:col-span-7">
                <div className="relative aspect-[4/3] w-full overflow-hidden rounded-3xl border-2 border-blue-500/50 bg-slate-950 shadow-2xl">
                  {usarSidecarLocal ? (
                    <img
                      src={streamMjpegUrl}
                      alt="Stream Biométrico"
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <video
                      ref={videoRef}
                      autoPlay
                      playsInline
                      muted
                      className="h-full w-full object-cover -scale-x-100"
                    />
                  )}

                  {/* Guia oval de enquadramento facial */}
                  <div className="pointer-events-none absolute inset-0 flex items-center justify-center">
                    <div className="h-64 w-52 rounded-full border-2 border-dashed border-emerald-400/80 shadow-[0_0_0_9999px_rgba(2,6,23,0.45)]" />
                  </div>

                  <div className="absolute bottom-3 left-3 right-3 flex items-center justify-between rounded-xl bg-slate-950/80 backdrop-blur px-3.5 py-2 text-xs text-slate-300">
                    <span className="flex items-center gap-2 font-semibold">
                      <span className="h-2.5 w-2.5 rounded-full bg-emerald-400 animate-ping" />
                      Posicione seu rosto no centro da marcação
                    </span>
                    <span className="font-mono text-[11px] text-slate-400">
                      {usarSidecarLocal ? 'Sidecar 8001' : 'Câmera Local'}
                    </span>
                  </div>
                </div>

                {/* Seletor de câmera caso haja mais de uma */}
                {!usarSidecarLocal && camerasWebRtc.length > 1 && (
                  <div className="mt-3 flex items-center gap-2">
                    <span className="text-xs text-slate-400">Câmera:</span>
                    <select
                      value={selectedDeviceId}
                      onChange={(e) => setSelectedDeviceId(e.target.value)}
                      className="rounded-lg border border-slate-700 bg-slate-900 px-3 py-1.5 text-xs text-slate-200"
                    >
                      {camerasWebRtc.map((cam, idx) => (
                        <option key={cam.deviceId} value={cam.deviceId}>
                          {cam.label || `Câmera ${idx + 1}`}
                        </option>
                      ))}
                    </select>
                  </div>
                )}
              </div>

              {/* Painel de Ação Biométrica */}
              <div className="md:col-span-5 space-y-5">
                <div className="rounded-3xl border border-slate-800 bg-slate-900/80 p-6 space-y-4">
                  <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-400">
                    <Camera className="h-6 w-6" />
                  </div>
                  <h3 className="text-xl font-extrabold text-white">
                    Confirmação de Identidade Facial
                  </h3>
                  <p className="text-sm text-slate-300 leading-relaxed">
                    Para proteger seus dados pessoais (LGPD), olhe diretamente para a câmera sem
                    boné ou óculos escuros e toque no botão abaixo.
                  </p>

                  {erro && (
                    <div className="rounded-xl border border-rose-500/40 bg-rose-950/50 p-3.5 text-xs text-rose-200 flex items-start gap-2.5">
                      <AlertTriangle className="h-4 w-4 text-rose-400 shrink-0 mt-0.5" />
                      <span>{erro}</span>
                    </div>
                  )}

                  <button
                    type="button"
                    disabled={loading || !cameraPronta}
                    onClick={handleCapturarEValidarBiometria}
                    className="w-full h-16 rounded-2xl bg-emerald-600 hover:bg-emerald-500 active:scale-[0.99] disabled:bg-slate-800 disabled:text-slate-500 text-lg font-extrabold text-white shadow-lg shadow-emerald-900/40 flex items-center justify-center gap-3 transition"
                  >
                    {loading ? (
                      <>
                        <RefreshCw className="h-5 w-5 animate-spin" />
                        Verificando Biometria...
                      </>
                    ) : (
                      <>
                        <Sparkles className="h-5 w-5" />
                        Capturar e Consultar Prontuário
                      </>
                    )}
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ============================================================
            ETAPA 3: PRONTUÁRIO DE CONSULTA DO APENADO
           ============================================================ */}
        {etapa === 'ETAPA_3_PRONTUARIO' && prontuario && (
          <div className="space-y-6">
            {/* Barra de Sessão Temporária LGPD (Oculta na impressão) */}
            <div className="no-print flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-emerald-500/40 bg-emerald-950/30 px-6 py-3.5">
              <div className="flex items-center gap-3">
                <CheckCircle2 className="h-6 w-6 text-emerald-400" />
                <div>
                  <span className="text-sm font-bold text-white">
                    Identidade Confirmada por Biometria Facial
                  </span>
                  {similaridadeObtida !== null && (
                    <span className="ml-2 rounded-full bg-emerald-500/20 px-2.5 py-0.5 text-xs font-mono text-emerald-300">
                      {(similaridadeObtida * 100).toFixed(1)}% de compatibilidade
                    </span>
                  )}
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="flex items-center gap-2 rounded-xl bg-slate-900/90 border border-slate-700 px-3.5 py-1.5 text-xs font-semibold text-amber-300">
                  <Clock className="h-4 w-4" />
                  <span>Encerramento automático em {segundosRestantes}s</span>
                  <button
                    type="button"
                    onClick={() => setSegundosRestantes((s) => Math.min(s + 30, 90))}
                    className="ml-1 underline text-blue-300 hover:text-blue-200"
                  >
                    +30s
                  </button>
                </div>

                <button
                  type="button"
                  onClick={handleImprimirComprovante}
                  className="inline-flex items-center gap-2 rounded-xl border border-blue-500/40 bg-blue-600/20 hover:bg-blue-600/30 px-4 py-2 text-xs font-bold text-blue-200 transition"
                >
                  <Printer className="h-4 w-4" />
                  Imprimir Comprovante
                </button>

                <button
                  type="button"
                  onClick={reiniciarFluxo}
                  className="inline-flex items-center gap-2 rounded-xl bg-rose-600 hover:bg-rose-500 px-4 py-2 text-xs font-bold text-white shadow transition"
                >
                  <LogOut className="h-4 w-4" />
                  Finalizar Consulta
                </button>
              </div>
            </div>

            {/* Cabeçalho exclusivo para folha impressa */}
            <div className="hidden print-only border-b-2 border-slate-800 pb-4 mb-4">
              <h1 className="text-xl font-bold">
                GOVERNO DO ESTADO DE SÃO PAULO — CPMA (COMPROVANTE DE CUMPRIMENTO DE PENA)
              </h1>
              <p className="text-xs">
                Emitido no Terminal de Autoatendimento em {new Date().toLocaleString('pt-BR')}
              </p>
            </div>

            {/* Cartão de Identificação + Progresso da Pena */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
              {/* Perfil do Apenado */}
              <div className="lg:col-span-4 rounded-3xl border border-slate-800 bg-slate-900/80 p-6 flex flex-col justify-between space-y-4">
                <div className="flex items-center gap-4">
                  {prontuario.usuario.foto ? (
                    <img
                      src={prontuario.usuario.foto}
                      alt={prontuario.usuario.nome}
                      className="h-20 w-20 rounded-2xl object-cover border-2 border-emerald-500/50"
                    />
                  ) : (
                    <div className="h-20 w-20 rounded-2xl bg-slate-800 flex items-center justify-center text-2xl font-bold text-slate-400">
                      {prontuario.usuario.nome.charAt(0)}
                    </div>
                  )}
                  <div>
                    <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">
                      Prestador / Apenado
                    </span>
                    <h3 className="text-xl font-extrabold text-white">
                      {prontuario.usuario.nome}
                    </h3>
                    <p className="text-xs font-mono text-slate-300 mt-0.5">
                      CPF: {mascararCpf(prontuario.usuario.cpf)}
                    </p>
                  </div>
                </div>

                <div className="space-y-2.5 border-t border-slate-800 pt-4 text-sm">
                  <div className="flex items-center justify-between">
                    <span className="text-slate-400">Nº do Processo:</span>
                    <span className="font-mono font-bold text-slate-200">
                      {prontuario.usuario.processoNumero || 'Não informado'}
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-slate-400">Instituição Parceira:</span>
                    <span className="font-semibold text-blue-300 text-right">
                      {prontuario.resumo?.nomeInstituicao ||
                        prontuario.penaPrincipal?.nomeInstituicao ||
                        'A definir'}
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-slate-400">Início da Pena:</span>
                    <span className="font-semibold text-slate-200">
                      {formatarDataBr(prontuario.penaPrincipal?.dataInicio)}
                    </span>
                  </div>
                </div>
              </div>

              {/* Resumo de Horas e Barra de Progresso */}
              <div className="lg:col-span-8 rounded-3xl border border-slate-800 bg-slate-900/80 p-6 space-y-5">
                {prontuario.penaPrincipal ? (
                  <>
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div>
                        <span className="text-xs font-bold uppercase tracking-wider text-blue-400">
                          Acompanhamento de Pena Alternativa
                        </span>
                        <h3 className="text-xl font-extrabold text-white flex items-center gap-2">
                          <Building2 className="h-5 w-5 text-blue-400" />
                          {prontuario.resumo?.nomeInstituicao ||
                            prontuario.penaPrincipal.nomeInstituicao ||
                            `Instituição #${prontuario.penaPrincipal.instituicaoId}`}
                        </h3>
                      </div>

                      <span
                        className={`rounded-full px-3.5 py-1 text-xs font-extrabold uppercase tracking-wider ${
                          prontuario.resumo?.status === 'CONCLUIDA' ||
                          prontuario.resumo?.statusCumprimento === 'CONCLUIDA'
                            ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                            : 'bg-blue-500/20 text-blue-300 border border-blue-500/40'
                        }`}
                      >
                        {prontuario.resumo?.status === 'CONCLUIDA' ||
                        prontuario.resumo?.statusCumprimento === 'CONCLUIDA'
                          ? 'Pena Concluída'
                          : 'Em Cumprimento'}
                      </span>
                    </div>

                    {/* Cards Numéricos */}
                    <div className="grid grid-cols-3 gap-4">
                      <div className="rounded-2xl border border-slate-800 bg-slate-950/70 p-4">
                        <span className="text-xs text-slate-400">Total a Cumprir</span>
                        <p className="text-2xl font-extrabold text-white mt-1">
                          {prontuario.resumo?.horasTotais ??
                            prontuario.resumo?.horasTotaisACumprir ??
                            prontuario.penaPrincipal.horasCumprir}
                          h
                        </p>
                      </div>

                      <div className="rounded-2xl border border-emerald-500/30 bg-emerald-950/25 p-4">
                        <span className="text-xs text-emerald-300">Horas Cumpridas</span>
                        <p className="text-2xl font-extrabold text-emerald-400 mt-1">
                          {prontuario.resumo?.horasCumpridas ??
                            prontuario.penaPrincipal.horasCumpridas}
                          h
                        </p>
                      </div>

                      <div className="rounded-2xl border border-amber-500/30 bg-amber-950/25 p-4">
                        <span className="text-xs text-amber-300">Horas Restantes</span>
                        <p className="text-2xl font-extrabold text-amber-400 mt-1">
                          {prontuario.resumo?.horasRestantes ??
                            Math.max(
                              0,
                              (prontuario.penaPrincipal.horasCumprir || 0) -
                                (prontuario.penaPrincipal.horasCumpridas || 0),
                            )}
                          h
                        </p>
                      </div>
                    </div>

                    {/* Barra de Progresso */}
                    <div className="space-y-2">
                      <div className="flex items-center justify-between text-xs font-bold">
                        <span className="text-slate-300 flex items-center gap-1.5">
                          <Award className="h-4 w-4 text-emerald-400" />
                          Progresso Total de Cumprimento
                        </span>
                        <span className="text-emerald-400 text-sm">
                          {(prontuario.resumo?.percentualConcluido ?? 0).toFixed(1)}%
                        </span>
                      </div>
                      <div className="h-4 w-full overflow-hidden rounded-full bg-slate-800 p-0.5">
                        <div
                          className="h-full rounded-full bg-gradient-to-r from-blue-500 to-emerald-400 transition-all duration-500"
                          style={{
                            width: `${Math.min(
                              100,
                              Math.max(0, prontuario.resumo?.percentualConcluido ?? 0),
                            )}%`,
                          }}
                        />
                      </div>
                    </div>

                    {prontuario.penaPrincipal.atividadesAcordadas && (
                      <div className="rounded-2xl border border-slate-800 bg-slate-950/50 p-3.5 text-xs text-slate-300">
                        <span className="font-bold text-slate-200">Atividades Acordadas: </span>
                        {prontuario.penaPrincipal.atividadesAcordadas}
                      </div>
                    )}
                  </>
                ) : (
                  <div className="h-full flex flex-col items-center justify-center text-center py-8 text-slate-400">
                    <FileText className="h-10 w-10 mb-2 opacity-50" />
                    <p className="font-bold text-slate-200">Nenhuma pena ativa vinculada</p>
                    <p className="text-xs">Procure o agente responsável para mais informações.</p>
                  </div>
                )}
              </div>
            </div>

            {/* Histórico de Registros de Comparecimento / Frequência */}
            <div className="rounded-3xl border border-slate-800 bg-slate-900/80 p-6 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-base font-extrabold text-white flex items-center gap-2">
                  <Calendar className="h-5 w-5 text-blue-400" />
                  Histórico de Dias Trabalhados ({prontuario.registros.length} registros)
                </h3>
              </div>

              {prontuario.registros.length === 0 ? (
                <p className="text-sm text-slate-400 py-4 text-center">
                  Ainda não há registros de trabalho lançados para esta pena.
                </p>
              ) : (
                <div className="max-h-56 overflow-y-auto rounded-2xl border border-slate-800">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-950 text-xs uppercase text-slate-400 sticky top-0">
                      <tr>
                        <th className="px-4 py-3">Data do Comparecimento</th>
                        <th className="px-4 py-3">Entrada</th>
                        <th className="px-4 py-3">Saída</th>
                        <th className="px-4 py-3 text-right">Horas Computadas</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-800/80 bg-slate-900/40">
                      {prontuario.registros.map((reg) => (
                        <tr key={reg.id} className="hover:bg-slate-800/40">
                          <td className="px-4 py-2.5 font-semibold text-slate-200">
                            {formatarDataBr(reg.dataTrabalho)}
                          </td>
                          <td className="px-4 py-2.5 font-mono text-slate-300">
                            {reg.horaInicio?.slice(0, 5)}
                          </td>
                          <td className="px-4 py-2.5 font-mono text-slate-300">
                            {reg.horaFim?.slice(0, 5)}
                          </td>
                          <td className="px-4 py-2.5 text-right font-mono font-bold text-emerald-400">
                            +{reg.horasCumpridasDia ?? 0}h
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        )}
      </main>

      {/* RODAPÉ DO TOTEM */}
      <footer className="no-print border-t border-slate-900 bg-slate-950 px-6 py-3 flex items-center justify-between text-xs text-slate-500">
        <span>
          CPMA Totem • Terminal ID: <strong className="text-slate-400">{config.terminalId}</strong>
        </span>
        <span>
          Atalho do Agente: <kbd className="rounded bg-slate-900 px-1.5 py-0.5 text-slate-400">Ctrl+Shift+C</kbd> (Rede) •{' '}
          <kbd className="rounded bg-slate-900 px-1.5 py-0.5 text-slate-400">F11</kbd> (Tela Cheia)
        </span>
      </footer>
    </div>
  );
};
