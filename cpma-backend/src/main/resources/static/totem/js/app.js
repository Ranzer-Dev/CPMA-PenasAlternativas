const PIN_LENGTH = 6;
const TERMINAL_ID = 'TOTEM_EXTERNO_01';
const TERMINAL_KEY = 'CPMA-TOTEM-KIOSK-TERMINAL-01-SP-GOV-AUTH-2026';
const FACIAL_SERVICE_URL = 'http://127.0.0.1:8001';

const state = {
  currentPin: '',
  token: null,
  tokenSessao: null,
  usuarioId: null,
  penaId: null,
  cameraStream: null,
  usandoMjpeg: false,
  timeoutTimer: null,
  timeoutSeconds: 30,
  listaCameras: [],
  cameraAtivaIndex: 0
};

const dom = {
  stepKeypad: document.getElementById('stepKeypad'),
  stepCamera: document.getElementById('stepCamera'),
  stepProntuario: document.getElementById('stepProntuario'),
  pinSlots: document.querySelectorAll('.pin-slot'),
  btnConfirmarCodigo: document.getElementById('btnConfirmarCodigo'),
  alertKeypad: document.getElementById('alertKeypad'),
  alertCamera: document.getElementById('alertCamera'),
  btnAlternarCamera: document.getElementById('btnAlternarCamera'),
  btnReconectarCamera: document.getElementById('btnReconectarCamera'),
  cameraFeed: document.getElementById('cameraFeed'),
  cameraFeedMjpeg: document.getElementById('cameraFeedMjpeg'),
  btnCapturarFoto: document.getElementById('btnCapturarFoto'),
  btnVoltarKeypad: document.getElementById('btnVoltarKeypad'),
  btnIrParaCamera: document.getElementById('btnIrParaCamera'),
  btnImprimir: document.getElementById('btnImprimir'),
  btnNovoAcesso: document.getElementById('btnNovoAcesso'),
  lblNome: document.getElementById('lblNome'),
  lblCodigo: document.getElementById('lblCodigo'),
  lblCpf: document.getElementById('lblCpf'),
  lblTelefone: document.getElementById('lblTelefone'),
  lblEndereco: document.getElementById('lblEndereco'),
  lblTipoPena: document.getElementById('lblTipoPena'),
  lblInstituicao: document.getElementById('lblInstituicao'),
  lblHorasTotais: document.getElementById('lblHorasTotais'),
  lblDataInicio: document.getElementById('lblDataInicio'),
  statPrevisto: document.getElementById('statPrevisto'),
  statCumpridas: document.getElementById('statCumpridas'),
  statRestantes: document.getElementById('statRestantes'),
  statConclusao: document.getElementById('statConclusao'),
  progressFill: document.getElementById('progressFill'),
  tabelaRegistrosCorpo: document.getElementById('tabelaRegistrosCorpo'),
  timeoutFill: document.getElementById('timeoutFill'),
  timeoutContador: document.getElementById('timeoutContador'),
  badgeOnline: document.getElementById('badgeOnline'),
  imgFotoApenado: document.getElementById('imgFotoApenado')
};

function init() {
  setupKioskLockdown();
  setupKeypad();
  setupCameraButtons();
  setupProntuarioActions();
  verificarStatusServidor();
  setInterval(verificarStatusServidor, 15000);
}

function setupKioskLockdown() {
  document.addEventListener('contextmenu', e => e.preventDefault());
  document.addEventListener('selectstart', e => e.preventDefault());
  document.addEventListener('dragstart', e => e.preventDefault());

  window.addEventListener('wheel', (e) => {
    if (e.ctrlKey) {
      e.preventDefault();
    }
  }, { passive: false });

  function aplicarLockdownTeclado() {
    if ('keyboard' in navigator && typeof navigator.keyboard.lock === 'function') {
      navigator.keyboard.lock(['Escape', 'F11', 'F12', 'KeyW', 'KeyN', 'KeyT', 'BrowserBack', 'BrowserForward']).catch(() => {});
    }
  }

  function requisitarFullscreen() {
    if (!document.fullscreenElement && document.documentElement.requestFullscreen) {
      document.documentElement.requestFullscreen().then(() => {
        aplicarLockdownTeclado();
      }).catch(() => {});
    }
  }

  document.addEventListener('click', requisitarFullscreen, { once: true });
  document.addEventListener('touchstart', requisitarFullscreen, { once: true });

  document.addEventListener('fullscreenchange', () => {
    if (!document.fullscreenElement) {
      setTimeout(requisitarFullscreen, 100);
    } else {
      aplicarLockdownTeclado();
    }
  });

  window.addEventListener('keydown', (e) => {
    const key = e.key;
    const lowerKey = key ? key.toLowerCase() : '';

    if (['Escape', 'F1', 'F2', 'F3', 'F4', 'F5', 'F6', 'F7', 'F8', 'F9', 'F10', 'F11', 'F12'].includes(key)) {
      e.preventDefault();
      e.stopPropagation();
      return;
    }

    if (e.ctrlKey && ['r', 'u', 'p', 's', 'o', 'h', 'j', 'n', 't', 'w', '+', '-', '0'].includes(lowerKey)) {
      e.preventDefault();
      e.stopPropagation();
      return;
    }

    if (e.ctrlKey && e.shiftKey && ['i', 'j', 'c', 'delete'].includes(lowerKey)) {
      e.preventDefault();
      e.stopPropagation();
      return;
    }

    if (e.altKey && ['f4', 'arrowleft', 'arrowright', 'home'].includes(lowerKey)) {
      e.preventDefault();
      e.stopPropagation();
      return;
    }
  }, true);
}


function setupKeypad() {
  document.querySelectorAll('.key-btn[data-num]').forEach(btn => {
    btn.addEventListener('click', () => {
      appendDigit(btn.getAttribute('data-num'));
    });
  });

  const btnBackspace = document.getElementById('btnBackspace');
  if (btnBackspace) {
    btnBackspace.addEventListener('click', backspaceDigit);
  }

  const btnClear = document.getElementById('btnClear');
  if (btnClear) {
    btnClear.addEventListener('click', clearDigits);
  }

  dom.btnConfirmarCodigo.addEventListener('click', handleValidarCodigo);

  window.addEventListener('keydown', (e) => {
    if (dom.stepKeypad.classList.contains('active')) {
      if (e.key >= '0' && e.key <= '9') {
        appendDigit(e.key);
      } else if (e.key === 'Backspace') {
        backspaceDigit();
      } else if (e.key === 'Enter') {
        if (state.currentPin.length === PIN_LENGTH) {
          handleValidarCodigo();
        }
      }
    }
  });
}

function appendDigit(digit) {
  if (state.currentPin.length < PIN_LENGTH) {
    state.currentPin += digit;
    updatePinDisplay();
  }
}

function backspaceDigit() {
  if (state.currentPin.length > 0) {
    state.currentPin = state.currentPin.slice(0, -1);
    updatePinDisplay();
  }
}

function clearDigits() {
  state.currentPin = '';
  updatePinDisplay();
}

function updatePinDisplay() {
  dom.pinSlots.forEach((slot, index) => {
    if (index < state.currentPin.length) {
      slot.textContent = state.currentPin[index];
      slot.classList.add('filled');
    } else {
      slot.textContent = '';
      slot.classList.remove('filled');
    }
  });

  dom.btnConfirmarCodigo.disabled = state.currentPin.length !== PIN_LENGTH;
  hideAlert(dom.alertKeypad);
}

async function handleValidarCodigo() {
  if (state.currentPin.length !== PIN_LENGTH) return;

  dom.btnConfirmarCodigo.disabled = true;
  dom.btnConfirmarCodigo.textContent = 'Validando Código...';
  hideAlert(dom.alertKeypad);

  try {
    const res = await fetch('/api/v1/totem/etapa1/validar-codigo', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Totem-Terminal-Key': TERMINAL_KEY
      },
      body: JSON.stringify({
        codigo: state.currentPin,
        terminalId: TERMINAL_ID
      })
    });

    const data = await res.json();

    if (!res.ok || !data.sucesso) {
      const msg = data.mensagem || data.message || 'Código de acesso incorreto ou expirado.';
      showAlert(dom.alertKeypad, 'error', msg);
      dom.btnConfirmarCodigo.disabled = false;
      dom.btnConfirmarCodigo.textContent = 'Avançar para Reconhecimento Facial ➔';
      return;
    }

    state.codigo = data.codigo;
    state.usuarioNome = data.usuarioNome;
    state.usuarioId = data.usuarioId;

    const badge = document.getElementById('badgeTitularIdentificado');
    if (badge) {
      badge.textContent = `Titular: ${data.usuarioNome} (Código: ${data.codigo})`;
    }

    abrirEtapaCamera();
  } catch (err) {
    showAlert(dom.alertKeypad, 'error', 'Falha ao conectar com o servidor. Verifique a rede.');
  } finally {
    dom.btnConfirmarCodigo.disabled = state.currentPin.length !== PIN_LENGTH;
    dom.btnConfirmarCodigo.textContent = 'Avançar para Reconhecimento Facial';
  }
}

function setupCameraButtons() {
  if (dom.btnIrParaCamera) {
    dom.btnIrParaCamera.addEventListener('click', () => {
      abrirEtapaCamera();
    });
  }

  if (dom.btnAlternarCamera) {
    dom.btnAlternarCamera.addEventListener('click', () => {
      alternarProximaCamera();
    });
  }

  if (dom.btnReconectarCamera) {
    dom.btnReconectarCamera.addEventListener('click', () => {
      iniciarCameraSilenciosa();
    });
  }

  dom.btnVoltarKeypad.addEventListener('click', () => {
    pararCamera();
    abrirEtapaKeypad();
  });

  dom.btnCapturarFoto.addEventListener('click', handleCapturarFoto);
}

async function obterCandidatosDeCamera() {
  const candidatos = [];
  state.listaCameras = [];
  if (navigator.mediaDevices && navigator.mediaDevices.enumerateDevices) {
    try {
      const devices = await navigator.mediaDevices.enumerateDevices();
      const videoDevices = devices.filter(d => d.kind === 'videoinput');
      state.listaCameras = videoDevices;

      if (dom.btnAlternarCamera) {
        dom.btnAlternarCamera.style.display = videoDevices.length > 1 ? 'block' : 'none';
      }

      const prioridade = [];
      const comuns = [];
      videoDevices.forEach(d => {
        const label = (d.label || '').toLowerCase();
        if (label.includes('iriun') || label.includes('droidcam') || label.includes('external') || label.includes('usb')) {
          prioridade.push(d);
        } else {
          comuns.push(d);
        }
      });
      const ordenados = [...prioridade, ...comuns];
      ordenados.forEach(dev => {
        if (dev.deviceId) {
          candidatos.push({ video: { deviceId: { exact: dev.deviceId }, width: { ideal: 640 }, height: { ideal: 480 } } });
          candidatos.push({ video: { deviceId: dev.deviceId } });
        }
      });
    } catch (err) {
      console.error(err);
    }
  }
  candidatos.push({ video: { width: { ideal: 640 }, height: { ideal: 480 } } });
  candidatos.push({ video: true });
  return candidatos;
}

async function alternarProximaCamera() {
  if (!state.listaCameras || state.listaCameras.length <= 1) {
    return;
  }
  state.cameraAtivaIndex = (state.cameraAtivaIndex + 1) % state.listaCameras.length;
  const proxDev = state.listaCameras[state.cameraAtivaIndex];
  if (!proxDev || !proxDev.deviceId) {
    return;
  }
  pararCamera();
  hideAlert(dom.alertCamera);
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { deviceId: { exact: proxDev.deviceId }, width: { ideal: 640 }, height: { ideal: 480 } }
    });
    state.cameraStream = stream;
    dom.cameraFeed.srcObject = state.cameraStream;
    try {
      await dom.cameraFeed.play();
    } catch (e) {}
  } catch (err) {
    iniciarCameraSilenciosa();
  }
}

async function iniciarCameraSilenciosa() {
  pararCamera();
  hideAlert(dom.alertCamera);
  if (dom.btnReconectarCamera) {
    dom.btnReconectarCamera.style.display = 'none';
  }

  try {
    const resHealth = await fetch(`${FACIAL_SERVICE_URL}/health`, { signal: AbortSignal.timeout(1200) });
    if (resHealth.ok) {
      state.usandoMjpeg = true;
      if (dom.cameraFeed) dom.cameraFeed.style.display = 'none';
      if (dom.cameraFeedMjpeg) {
        dom.cameraFeedMjpeg.src = `${FACIAL_SERVICE_URL}/camera/stream?t=` + Date.now();
        dom.cameraFeedMjpeg.style.display = 'block';
      }
      if (dom.btnAlternarCamera) dom.btnAlternarCamera.style.display = 'none';
      return;
    }
  } catch (e) {
    state.usandoMjpeg = false;
  }

  if (dom.cameraFeedMjpeg) {
    dom.cameraFeedMjpeg.src = '';
    dom.cameraFeedMjpeg.style.display = 'none';
  }
  if (dom.cameraFeed) {
    dom.cameraFeed.style.display = 'block';
  }

  const candidatos = await obterCandidatosDeCamera();
  let streamObtido = null;
  let ultimoErro = null;

  for (const constraints of candidatos) {
    try {
      streamObtido = await navigator.mediaDevices.getUserMedia(constraints);
      if (streamObtido) {
        break;
      }
    } catch (err) {
      ultimoErro = err;
    }
  }

  if (streamObtido) {
    state.cameraStream = streamObtido;
    dom.cameraFeed.srcObject = state.cameraStream;
    try {
      await dom.cameraFeed.play();
    } catch (e) {}
    return;
  }

  const isLockOrBusy = ultimoErro && (
    ultimoErro.name === 'NotReadableError' ||
    ultimoErro.name === 'TrackStartError' ||
    (ultimoErro.message && ultimoErro.message.toLowerCase().includes('could not start video source'))
  );

  let mensagemAmigavel = 'Não foi possível acessar a câmera do totem no momento.';
  if (isLockOrBusy) {
    mensagemAmigavel = 'A câmera está temporariamente ocupada ou inacessível. Se outro aplicativo estiver aberto no sistema, feche-o e clique em Tentar Novamente.';
  }

  showAlert(dom.alertCamera, 'error', mensagemAmigavel);
  if (dom.btnReconectarCamera) {
    dom.btnReconectarCamera.style.display = 'block';
  }
}

async function abrirEtapaCamera() {
  mudarEtapa(dom.stepCamera);
  hideAlert(dom.alertCamera);
  dom.btnCapturarFoto.disabled = false;
  dom.btnCapturarFoto.textContent = 'Confirmar Identidade Facial';
  await iniciarCameraSilenciosa();
}

function pararCamera() {
  if (dom.cameraFeedMjpeg) {
    dom.cameraFeedMjpeg.src = '';
    dom.cameraFeedMjpeg.style.display = 'none';
  }
  state.usandoMjpeg = false;
  if (state.cameraStream) {
    state.cameraStream.getTracks().forEach(track => track.stop());
    state.cameraStream = null;
  }
  if (dom.cameraFeed) {
    dom.cameraFeed.srcObject = null;
    dom.cameraFeed.style.display = 'none';
  }
}

async function handleCapturarFoto() {
  dom.btnCapturarFoto.disabled = true;
  dom.btnCapturarFoto.textContent = 'Processando Biometria 1:1...';
  hideAlert(dom.alertCamera);

  let base64Data = null;

  if (state.usandoMjpeg) {
    try {
      const resCap = await fetch(`${FACIAL_SERVICE_URL}/camera/capture`);
      if (resCap.ok) {
        const dataCap = await resCap.json();
        base64Data = dataCap.rawBase64 || dataCap.base64;
      }
    } catch (err) {
      console.error(err);
    }
  }

  if (!base64Data) {
    const canvas = document.createElement('canvas');
    const sourceEl = (state.usandoMjpeg && dom.cameraFeedMjpeg) ? dom.cameraFeedMjpeg : dom.cameraFeed;
    canvas.width = sourceEl.videoWidth || sourceEl.naturalWidth || 640;
    canvas.height = sourceEl.videoHeight || sourceEl.naturalHeight || 480;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(sourceEl, 0, 0, canvas.width, canvas.height);
    base64Data = canvas.toDataURL('image/jpeg', 0.9);
  }

  try {
    const res = await fetch('/api/v1/totem/etapa2/validar-biometria', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Totem-Terminal-Key': TERMINAL_KEY
      },
      body: JSON.stringify({
        codigo: state.codigo,
        fotoCapturadaBase64: base64Data,
        terminalId: TERMINAL_ID,
        threshold: 0.65
      })
    });

    const data = await res.json();

    if (!res.ok || !data.sucesso) {
      const msg = data.mensagem || data.message || `Biometria facial não corresponde ao titular do código (${state.usuarioNome}).`;
      showAlert(dom.alertCamera, 'error', msg);
      dom.btnCapturarFoto.disabled = false;
      dom.btnCapturarFoto.textContent = 'Tentar Novamente';
      return;
    }

    state.usuarioId = data.usuarioId;
    state.tokenSessao = data.tokenSessao;
    pararCamera();
    abrirEtapaProntuario();
  } catch (err) {
    showAlert(dom.alertCamera, 'error', 'Erro ao validar biometria: ' + err.message);
    dom.btnCapturarFoto.disabled = false;
    dom.btnCapturarFoto.textContent = 'Tentar Novamente';
  }
}

async function abrirEtapaProntuario() {
  mudarEtapa(dom.stepProntuario);
  iniciarTimerInatividade();
  await carregarDadosCompletosProntuario();
}

async function carregarDadosCompletosProntuario() {
  try {
    const authHeaders = state.tokenSessao ? { 'Authorization': 'Bearer ' + state.tokenSessao } : {};

    const resUser = await fetch('/api/v1/usuarios/' + state.usuarioId, { headers: authHeaders });
    if (resUser.ok) {
      const u = await resUser.json();
      dom.lblNome.textContent = u.nome || '-';
      dom.lblCodigo.textContent = u.codigo || '-';
      dom.lblCpf.textContent = formatarCpf(u.cpf);
      dom.lblTelefone.textContent = u.telefone || '-';
      dom.lblEndereco.textContent = (u.endereco || '') + (u.bairro ? ' - ' + u.bairro : '');
      if (dom.imgFotoApenado) {
        if (u.foto && u.foto.trim()) {
          dom.imgFotoApenado.src = 'data:image/jpeg;base64,' + u.foto.trim();
        } else {
          dom.imgFotoApenado.src = "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%2364748b'><path d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/></svg>";
        }
      }
    }

    const resPenas = await fetch('/api/v1/penas/usuario/' + state.usuarioId, { headers: authHeaders });
    let penaAtiva = null;
    if (resPenas.ok) {
      const penas = await resPenas.json();
      if (penas && penas.length > 0) {
        if (state.penaId) {
          penaAtiva = penas.find(p => p.idPena === state.penaId);
        }
        if (!penaAtiva) {
          penaAtiva = penas[0];
          state.penaId = penaAtiva.idPena;
        }
      }
    }

    if (penaAtiva) {
      dom.lblTipoPena.textContent = penaAtiva.tipoPena || 'Prestação de Serviços à Comunidade (PSC)';
      dom.lblInstituicao.textContent = penaAtiva.instituicaoPrincipalNome || '-';
      const totalHorasPena = penaAtiva.horasTotais || 0;
      dom.lblHorasTotais.textContent = totalHorasPena + ' horas';
      dom.lblDataInicio.textContent = penaAtiva.dataInicio || '-';

      let horasPrevistas = totalHorasPena;
      let horasCumpridas = 0;

      const resRegs = await fetch('/api/v1/registros-trabalho/pena/' + penaAtiva.idPena, { headers: authHeaders });
      let registros = [];
      if (resRegs.ok) {
        registros = await resRegs.json();
        renderizarTabelaRegistros(registros);
        horasCumpridas = registros.reduce((acc, r) => acc + (r.horasCumpridas || 0), 0);
      }

      try {
        const resResumo = await fetch('/api/v1/registros-trabalho/pena/' + penaAtiva.idPena + '/resumo', { headers: authHeaders });
        if (resResumo.ok) {
          const r = await resResumo.json();
          if (r.horasTotais != null && r.horasTotais > 0) {
            horasPrevistas = r.horasTotais;
          }
          if (r.horasCumpridas != null) {
            horasCumpridas = r.horasCumpridas;
          }
        }
      } catch (errResumo) {}

      const horasRestantes = Math.max(0, horasPrevistas - horasCumpridas);
      const percConclusao = horasPrevistas > 0 ? (horasCumpridas / horasPrevistas) * 100 : 0;

      dom.statPrevisto.textContent = horasPrevistas.toFixed(0) + 'h';
      dom.statCumpridas.textContent = horasCumpridas.toFixed(1) + 'h';
      dom.statRestantes.textContent = horasRestantes.toFixed(1) + 'h';
      dom.statConclusao.textContent = percConclusao.toFixed(1) + '%';
      dom.progressFill.style.width = Math.min(100, percConclusao) + '%';

      const elPrintData = document.getElementById('lblDataEmissaoPrint');
      if (elPrintData) {
        elPrintData.textContent = new Date().toLocaleString('pt-BR');
      }
    }
  } catch (err) {
    console.error(err);
  }
}

function renderizarTabelaRegistros(registros) {
  dom.tabelaRegistrosCorpo.innerHTML = '';
  if (!registros || registros.length === 0) {
    const tr = document.createElement('tr');
    tr.innerHTML = '<td colspan="5" style="text-align: center; color: #94a3b8; padding: 18px;">Nenhum comparecimento anterior registrado.</td>';
    dom.tabelaRegistrosCorpo.appendChild(tr);
    return;
  }

  registros.forEach(r => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${r.dataTrabalho || '-'}</td>
      <td>${r.horarioInicio || '-'}</td>
      <td>${r.horarioSaida || '-'}</td>
      <td><strong>${r.horasCumpridas != null ? r.horasCumpridas.toFixed(1) + 'h' : '-'}</strong></td>
      <td>${r.atividades || '-'}</td>
    `;
    dom.tabelaRegistrosCorpo.appendChild(tr);
  });
}

function setupProntuarioActions() {
  dom.btnImprimir.addEventListener('click', () => {
    window.print();
  });

  dom.btnNovoAcesso.addEventListener('click', concluirAcesso);
}

function concluirAcesso() {
  pararTimerInatividade();
  pararCamera();
  clearDigits();
  state.token = null;
  state.tokenSessao = null;
  state.usuarioId = null;
  state.penaId = null;
  abrirEtapaKeypad();
}

function abrirEtapaKeypad() {
  mudarEtapa(dom.stepKeypad);
  clearDigits();
}

function iniciarTimerInatividade() {
  pararTimerInatividade();
  state.timeoutSeconds = 30;
  dom.timeoutContador.textContent = state.timeoutSeconds;
  dom.timeoutFill.style.width = '100%';

  state.timeoutTimer = setInterval(() => {
    state.timeoutSeconds--;
    dom.timeoutContador.textContent = state.timeoutSeconds;
    const perc = (state.timeoutSeconds / 30) * 100;
    dom.timeoutFill.style.width = perc + '%';

    if (state.timeoutSeconds <= 0) {
      concluirAcesso();
    }
  }, 1000);
}

function pararTimerInatividade() {
  if (state.timeoutTimer) {
    clearInterval(state.timeoutTimer);
    state.timeoutTimer = null;
  }
}

function mudarEtapa(etapaAtiva) {
  document.querySelectorAll('.step-card').forEach(card => card.classList.remove('active'));
  etapaAtiva.classList.add('active');
}

function showAlert(elem, type, message) {
  elem.className = 'alert-box ' + type;
  elem.textContent = message;
  elem.style.display = 'block';
}

function hideAlert(elem) {
  elem.style.display = 'none';
  elem.textContent = '';
}

function formatarCpf(cpf) {
  if (!cpf) return '-';
  const digits = cpf.replace(/\D/g, '');
  if (digits.length === 11) {
    return digits.substring(0, 3) + '.' + digits.substring(3, 6) + '.' + digits.substring(6, 9) + '-' + digits.substring(9, 11);
  }
  return cpf;
}

async function verificarStatusServidor() {
  try {
    const res = await fetch('/api/v1/totem/status');
    if (res.ok) {
      dom.badgeOnline.innerHTML = '● Sistema Online';
      dom.badgeOnline.className = 'badge-online';
    } else {
      dom.badgeOnline.innerHTML = '○ Servidor Offline';
      dom.badgeOnline.className = 'badge-terminal';
    }
  } catch (err) {
    dom.badgeOnline.innerHTML = '○ Servidor Offline';
    dom.badgeOnline.className = 'badge-terminal';
  }
}

document.addEventListener('DOMContentLoaded', init);
