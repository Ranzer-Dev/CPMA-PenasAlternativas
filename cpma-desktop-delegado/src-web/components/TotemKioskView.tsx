import React, { useState, useRef, useEffect } from 'react';
import { formatarCPF } from '../utils/formatters';
import {
  Smartphone,
  Camera,
  CheckCircle2,
  AlertCircle,
  RotateCcw,
  Shield,
  ArrowLeft,
  Clock,
  Sparkles,
} from 'lucide-react';

interface TotemKioskViewProps {
  onSairKiosk: () => void;
}

export const TotemKioskView: React.FC<TotemKioskViewProps> = ({ onSairKiosk }) => {
  // Step: 1 = PIN Keypad, 2 = Camera/Biometrics, 3 = Confirmation & Summary
  const [etapa, setEtapa] = useState<1 | 2 | 3>(1);
  const [pin, setPin] = useState<string>('');
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  // Data from stage 1
  const [dadosEtapa1, setDadosEtapa1] = useState<{
    idUsuario: number;
    nome: string;
    cpf: string;
    codigoPena: string;
    foto?: string;
    idCodigoAcesso: number;
    idPena?: number;
  } | null>(null);

  // Result from stage 2
  const [resultadoEtapa2, setResultadoEtapa2] = useState<{
    mensagem: string;
    resumo?: {
      horasCumpridas: number;
      horasTotais: number;
      horasFaltantes: number;
      percentual: number;
    };
  } | null>(null);

  // Camera stream
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const [cameraAtiva, setCameraAtiva] = useState(false);
  const [contagemAutoReset, setContagemAutoReset] = useState(15);

  // Keypad click handler
  const handleDigit = (digit: string) => {
    if (pin.length < 6) {
      setPin(prev => prev + digit);
      setErro(null);
    }
  };

  const handleBackspace = () => {
    setPin(prev => prev.slice(0, -1));
    setErro(null);
  };

  const handleLimpar = () => {
    setPin('');
    setErro(null);
  };

  // Submit Step 1: Validate PIN
  const handleValidarPin = async () => {
    if (pin.length !== 6) {
      setErro('Digite o código de 6 números completo.');
      return;
    }

    setCarregando(true);
    setErro(null);

    try {
      const res = await fetch('/api/v1/totem/etapa1/validar-codigo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ codigo: pin }),
      });

      const data = await res.json();
      if (!res.ok || !data.valido) {
        throw new Error(data.mensagem || 'Código de acesso inválido ou expirado.');
      }

      setDadosEtapa1(data);
      setEtapa(2);
    } catch (err: any) {
      setErro(err.message || 'Código não reconhecido. Verifique no balcão de atendimento.');
    } finally {
      setCarregando(false);
    }
  };

  // Start webcam in Step 2
  useEffect(() => {
    if (etapa === 2) {
      setCameraAtiva(true);
      navigator.mediaDevices.getUserMedia({ video: { width: 480, height: 480 } })
        .then(stream => {
          if (videoRef.current) {
            videoRef.current.srcObject = stream;
            videoRef.current.play();
          }
        })
        .catch(err => {
          console.warn('Webcam stream fallback:', err);
        });
    } else {
      if (videoRef.current && videoRef.current.srcObject) {
        const stream = videoRef.current.srcObject as MediaStream;
        stream.getTracks().forEach(t => t.stop());
      }
      setCameraAtiva(false);
    }
  }, [etapa]);

  // Step 2: Validate Biometrics & Register Attendance
  const handleValidarBiometria = async () => {
    if (!dadosEtapa1) return;
    setCarregando(true);
    setErro(null);

    try {
      // Capture canvas screenshot if camera stream available
      let imagemBase64 = dadosEtapa1.foto || '';
      if (videoRef.current) {
        const canvas = document.createElement('canvas');
        canvas.width = 320;
        canvas.height = 320;
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.drawImage(videoRef.current, 0, 0, 320, 320);
          imagemBase64 = canvas.toDataURL('image/jpeg', 0.8);
        }
      }

      const res = await fetch('/api/v1/totem/etapa2/validar-biometria', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          idUsuario: dadosEtapa1.idUsuario,
          idCodigoAcesso: dadosEtapa1.idCodigoAcesso,
          imagemBase64,
        }),
      });

      const data = await res.json();
      if (!res.ok || !data.autorizado) {
        throw new Error(data.mensagem || 'Falha na validação biométrica facial.');
      }

      setResultadoEtapa2(data);
      setEtapa(3);
    } catch (err: any) {
      setErro(err.message || 'Biometria não confirmada. Tente novamente.');
    } finally {
      setCarregando(false);
    }
  };

  // Step 3: Countdown auto-reset to Step 1
  useEffect(() => {
    if (etapa === 3) {
      setContagemAutoReset(15);
      const timer = setInterval(() => {
        setContagemAutoReset(prev => {
          if (prev <= 1) {
            clearInterval(timer);
            resetTotem();
            return 15;
          }
          return prev - 1;
        });
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [etapa]);

  const resetTotem = () => {
    setEtapa(1);
    setPin('');
    setDadosEtapa1(null);
    setResultadoEtapa2(null);
    setErro(null);
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white flex flex-col justify-between selection:bg-blue-500 selection:text-white">
      {/* Kiosk Header matching official totem kiosk */}
      <header className="bg-slate-950 border-b border-slate-800 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <img
            src="/images/logo-sp.png"
            alt="Governo de SP"
            className="h-10 w-auto object-contain hidden sm:block"
            onError={(e) => { (e.target as HTMLElement).style.display = 'none'; }}
          />
          <img
            src="/images/brasao-votorantim.png"
            alt="Votorantim"
            className="h-10 w-auto object-contain hidden sm:block"
            onError={(e) => { (e.target as HTMLElement).style.display = 'none'; }}
          />
          <div>
            <h1 className="text-xs sm:text-sm font-black tracking-wider uppercase text-blue-400">
              Governo do Estado de São Paulo
            </h1>
            <h2 className="text-sm sm:text-base font-bold text-white">
              Secretaria da Administração Penitenciária - CPMA
            </h2>
            <p className="text-2xs text-slate-400">
              Terminal Oficial de Autoatendimento e Ponto Biométrico Kiosk
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-3">
          <div className="hidden sm:flex flex-col items-end text-xs">
            <span className="font-mono text-slate-400">TOTEM_EXTERNO_01</span>
            <span className="text-emerald-400 flex items-center space-x-1 font-semibold">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              <span>Online</span>
            </span>
          </div>

          <button
            onClick={onSairKiosk}
            className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg text-xs font-bold border border-slate-700 flex items-center space-x-1 transition-colors cursor-pointer"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Voltar ao Admin</span>
          </button>
        </div>
      </header>

      {/* Main Kiosk Content */}
      <main className="flex-1 flex items-center justify-center p-4 sm:p-8">
        <div className="w-full max-w-xl bg-slate-800/90 rounded-3xl p-6 sm:p-10 border border-slate-700 shadow-2xl backdrop-blur-md">
          {/* STEP 1: PIN KEYPAD */}
          {etapa === 1 && (
            <div className="space-y-6 text-center">
              <div>
                <span className="px-3 py-1 bg-blue-500/20 text-blue-400 border border-blue-500/30 text-2xs font-bold rounded-full uppercase">
                  Etapa 1 de 2
                </span>
                <h3 className="text-2xl font-bold mt-2 text-white">Identificação do Apenado</h3>
                <p className="text-xs text-slate-400 mt-1 max-w-md mx-auto">
                  Digite no teclado virtual abaixo a senha de 6 números gerada no atendimento do balcão.
                </p>
              </div>

              {erro && (
                <div className="p-3 bg-red-900/40 border border-red-500/50 text-red-200 text-xs rounded-xl flex items-center justify-center space-x-2">
                  <AlertCircle className="w-4 h-4 shrink-0 text-red-400" />
                  <span>{erro}</span>
                </div>
              )}

              {/* 6-PIN Slot Display */}
              <div className="flex justify-center space-x-3 py-2">
                {[0, 1, 2, 3, 4, 5].map((idx) => {
                  const digit = pin[idx];
                  return (
                    <div
                      key={idx}
                      className={`w-12 h-14 rounded-2xl flex items-center justify-center text-2xl font-black font-mono border-2 transition-all ${
                        digit
                          ? 'border-blue-500 bg-blue-600/20 text-white shadow-lg shadow-blue-500/20'
                          : 'border-slate-700 bg-slate-900/60 text-slate-600'
                      }`}
                    >
                      {digit ? '•' : ''}
                    </div>
                  );
                })}
              </div>

              {/* Virtual Keypad Grid */}
              <div className="grid grid-cols-3 gap-3 max-w-xs mx-auto">
                {['1', '2', '3', '4', '5', '6', '7', '8', '9'].map((num) => (
                  <button
                    key={num}
                    type="button"
                    onClick={() => handleDigit(num)}
                    className="h-14 rounded-2xl bg-slate-700/60 hover:bg-slate-700 active:bg-blue-600 border border-slate-600/50 text-xl font-black text-white transition-all shadow-sm active:scale-95 cursor-pointer flex items-center justify-center"
                  >
                    {num}
                  </button>
                ))}

                <button
                  type="button"
                  onClick={handleLimpar}
                  className="h-14 rounded-2xl bg-slate-800 hover:bg-slate-700 text-xs font-bold text-slate-300 border border-slate-700 transition-all active:scale-95 cursor-pointer flex items-center justify-center"
                >
                  LIMPAR
                </button>

                <button
                  type="button"
                  onClick={() => handleDigit('0')}
                  className="h-14 rounded-2xl bg-slate-700/60 hover:bg-slate-700 active:bg-blue-600 border border-slate-600/50 text-xl font-black text-white transition-all shadow-sm active:scale-95 cursor-pointer flex items-center justify-center"
                >
                  0
                </button>

                <button
                  type="button"
                  onClick={handleBackspace}
                  className="h-14 rounded-2xl bg-slate-800 hover:bg-slate-700 text-xs font-bold text-slate-300 border border-slate-700 transition-all active:scale-95 cursor-pointer flex items-center justify-center"
                >
                  CORRIGIR
                </button>
              </div>

              <div className="pt-2">
                <button
                  type="button"
                  onClick={handleValidarPin}
                  disabled={carregando || pin.length !== 6}
                  className="w-full max-w-xs py-3.5 bg-blue-600 hover:bg-blue-500 disabled:opacity-40 text-white font-bold rounded-2xl text-sm transition-all shadow-lg shadow-blue-600/30 cursor-pointer disabled:cursor-not-allowed mx-auto"
                >
                  {carregando ? 'Validando PIN...' : 'Confirmar Senha'}
                </button>
              </div>
            </div>
          )}

          {/* STEP 2: BIOMETRIC RECOGNITION */}
          {etapa === 2 && dadosEtapa1 && (
            <div className="space-y-6 text-center">
              <div>
                <span className="px-3 py-1 bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 text-2xs font-bold rounded-full uppercase">
                  Etapa 2 de 2 • Biometria Facial
                </span>
                <h3 className="text-2xl font-bold mt-2 text-white">Validação Biométrica</h3>
                <p className="text-xs text-slate-300 mt-1">
                  Apenado identificado: <strong className="text-white">{dadosEtapa1.nome}</strong> (Cód: {dadosEtapa1.codigoPena})
                </p>
              </div>

              {erro && (
                <div className="p-3 bg-red-900/40 border border-red-500/50 text-red-200 text-xs rounded-xl flex items-center justify-center space-x-2">
                  <AlertCircle className="w-4 h-4 shrink-0 text-red-400" />
                  <span>{erro}</span>
                </div>
              )}

              {/* Camera Frame */}
              <div className="relative w-64 h-64 mx-auto rounded-3xl overflow-hidden border-4 border-blue-500/60 bg-slate-950 shadow-2xl flex items-center justify-center">
                {cameraAtiva ? (
                  <video ref={videoRef} className="w-full h-full object-cover" autoPlay playsInline />
                ) : (
                  <div className="space-y-2 text-slate-400 p-4">
                    <Camera className="w-12 h-12 mx-auto animate-pulse" />
                    <p className="text-xs">Iniciando câmera biométrica...</p>
                  </div>
                )}

                {/* Face Overlay Simulation Guide */}
                <div className="absolute inset-0 border-2 border-dashed border-white/30 rounded-full m-8 pointer-events-none flex items-center justify-center">
                  <span className="text-3xs text-white/60 bg-slate-900/60 px-2 py-0.5 rounded uppercase font-bold">
                    Enquadre o Rosto
                  </span>
                </div>
              </div>

              <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
                <button
                  type="button"
                  onClick={resetTotem}
                  className="w-full sm:w-auto px-5 py-3 bg-slate-700 hover:bg-slate-600 text-slate-300 rounded-2xl text-xs font-bold transition-all cursor-pointer"
                >
                  Cancelar
                </button>

                <button
                  type="button"
                  onClick={handleValidarBiometria}
                  disabled={carregando}
                  className="w-full sm:w-auto px-8 py-3.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-2xl text-sm font-bold shadow-lg shadow-emerald-600/30 transition-all flex items-center justify-center space-x-2 cursor-pointer disabled:opacity-50"
                >
                  <Camera className="w-4 h-4" />
                  <span>{carregando ? 'Processando Rosto...' : 'Validar e Registrar Ponto'}</span>
                </button>
              </div>
            </div>
          )}

          {/* STEP 3: SUCCESS CONFIRMATION & PENALTY SUMMARY */}
          {etapa === 3 && dadosEtapa1 && resultadoEtapa2 && (
            <div className="space-y-6 text-center animate-in zoom-in-95">
              <div className="w-16 h-16 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 flex items-center justify-center mx-auto">
                <CheckCircle2 className="w-10 h-10" />
              </div>

              <div>
                <h3 className="text-2xl font-black text-white">Presença Registrada com Sucesso!</h3>
                <p className="text-xs text-emerald-400 font-semibold mt-1">
                  Validação biométrica autenticada nos servidores CPMA
                </p>
              </div>

              {/* User badge */}
              <div className="bg-slate-900/80 p-4 rounded-2xl border border-slate-700/60 text-left flex items-center space-x-4">
                <div className="w-12 h-12 rounded-xl bg-slate-800 overflow-hidden shrink-0 border border-slate-700">
                  {dadosEtapa1.foto ? (
                    <img src={dadosEtapa1.foto} alt={dadosEtapa1.nome} className="w-full h-full object-cover" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center font-bold text-slate-400">
                      {dadosEtapa1.nome.charAt(0)}
                    </div>
                  )}
                </div>
                <div>
                  <h4 className="font-bold text-sm text-white">{dadosEtapa1.nome}</h4>
                  <p className="text-2xs text-slate-400 font-mono">
                    CPF: {formatarCPF(dadosEtapa1.cpf)} • Cód: {dadosEtapa1.codigoPena}
                  </p>
                </div>
              </div>

              {/* Progress Summary */}
              {resultadoEtapa2.resumo && (
                <div className="bg-slate-900/80 p-5 rounded-2xl border border-slate-700/60 space-y-3 text-left">
                  <div className="flex justify-between items-center text-xs">
                    <span className="font-bold text-slate-300">Cumprimento da Pena:</span>
                    <span className="font-black text-emerald-400 font-mono text-sm">
                      {resultadoEtapa2.resumo.percentual}%
                    </span>
                  </div>

                  <div className="w-full h-3 bg-slate-800 rounded-full overflow-hidden border border-slate-700">
                    <div
                      className="h-full bg-linear-to-r from-blue-500 to-emerald-400 rounded-full transition-all duration-700"
                      style={{ width: `${Math.min(100, resultadoEtapa2.resumo.percentual)}%` }}
                    ></div>
                  </div>

                  <div className="grid grid-cols-3 gap-2 pt-1 text-center">
                    <div className="bg-slate-800/80 p-2 rounded-xl">
                      <span className="text-3xs text-slate-400 uppercase block font-bold">Cumpridas</span>
                      <strong className="text-emerald-400 text-sm font-mono font-black">
                        {resultadoEtapa2.resumo.horasCumpridas}h
                      </strong>
                    </div>
                    <div className="bg-slate-800/80 p-2 rounded-xl">
                      <span className="text-3xs text-slate-400 uppercase block font-bold">Restantes</span>
                      <strong className="text-amber-400 text-sm font-mono font-black">
                        {resultadoEtapa2.resumo.horasFaltantes}h
                      </strong>
                    </div>
                    <div className="bg-slate-800/80 p-2 rounded-xl">
                      <span className="text-3xs text-slate-400 uppercase block font-bold">Total Pena</span>
                      <strong className="text-blue-400 text-sm font-mono font-black">
                        {resultadoEtapa2.resumo.horasTotais}h
                      </strong>
                    </div>
                  </div>
                </div>
              )}

              <div className="space-y-2 pt-2">
                <button
                  type="button"
                  onClick={resetTotem}
                  className="w-full py-3.5 bg-blue-600 hover:bg-blue-500 text-white font-bold rounded-2xl text-sm transition-all shadow-lg shadow-blue-600/30 cursor-pointer"
                >
                  Concluir Atendimento ({contagemAutoReset}s)
                </button>
              </div>
            </div>
          )}
        </div>
      </main>

      {/* Kiosk Footer */}
      <footer className="bg-slate-950 border-t border-slate-800 px-6 py-3 text-center text-xs text-slate-500">
        <span>Central de Penas e Medidas Alternativas • Votorantim / Sorocaba SP</span>
      </footer>
    </div>
  );
};
