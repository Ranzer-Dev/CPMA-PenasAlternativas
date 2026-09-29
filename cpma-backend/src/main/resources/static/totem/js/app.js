const PIN_LENGTH = 6;
const TERMINAL_ID = 'TOTEM_EXTERNO_01';

const state = {
  currentPin: '',
  token: null,
  usuarioId: null,
  penaId: null,
  cameraStream: null,
  timeoutTimer: null,
  timeoutSeconds: 30
};

const dom = {
  stepKeypad: document.getElementById('stepKeypad'),
  stepCamera: document.getElementById('stepCamera'),
  stepProntuario: document.getElementById('stepProntuario'),
  pinSlots: document.querySelectorAll('.pin-slot'),
  btnConfirmarCodigo: document.getElementById('btnConfirmarCodigo'),
  alertKeypad: document.getElementById('alertKeypad'),
  alertCamera: document.getElementById('alertCamera'),
  cameraFeed: document.getElementById('cameraFeed'),
  btnCapturarFoto: document.getElementById('btnCapturarFoto'),
  btnVoltarKeypad: document.getElementById('btnVoltarKeypad'),
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
  dom.btnConfirmarCodigo.textContent = 'Validando Acesso...';
  hideAlert(dom.alertKeypad);

  try {
    const res = await fetch('/api/v1/totem/validar-codigo', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        codigo: state.currentPin,
        codigoAcesso: state.currentPin,
        terminalId: TERMINAL_ID
      })
    });

    const data = await res.json();

    if (!res.ok || !data.sucesso) {
      const msg = data.mensagem || data.message || 'Código de acesso incorreto ou expirado.';
      showAlert(dom.alertKeypad, 'error', msg);
      dom.btnConfirmarCodigo.disabled = false;
      dom.btnConfirmarCodigo.textContent = 'Confirmar Acesso ➔';
      return;
    }

    state.usuarioId = data.usuarioId;
    abrirEtapaProntuario();
  } catch (err) {
    showAlert(dom.alertKeypad, 'error', 'Falha ao conectar com o servidor. Verifique a rede.');
  } finally {
    dom.btnConfirmarCodigo.disabled = state.currentPin.length !== PIN_LENGTH;
    dom.btnConfirmarCodigo.textContent = 'Confirmar Acesso ➔';
  }
}

function setupCameraButtons() {
  dom.btnVoltarKeypad.addEventListener('click', () => {
    pararCamera();
    abrirEtapaKeypad();
  });

  dom.btnCapturarFoto.addEventListener('click', handleCapturarFoto);
}

async function abrirEtapaCamera() {
  mudarEtapa(dom.stepCamera);
  hideAlert(dom.alertCamera);
  dom.btnCapturarFoto.disabled = false;
  dom.btnCapturarFoto.textContent = '📸 Capturar Foto e Validar';

  try {
    state.cameraStream = await navigator.mediaDevices.getUserMedia({
      video: { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' }
    });
    dom.cameraFeed.srcObject = state.cameraStream;
  } catch (err) {
    showAlert(dom.alertCamera, 'error', 'Não foi possível acessar a câmera do totem: ' + err.message);
  }
}

function pararCamera() {
  if (state.cameraStream) {
    state.cameraStream.getTracks().forEach(track => track.stop());
    state.cameraStream = null;
  }
  dom.cameraFeed.srcObject = null;
}

async function handleCapturarFoto() {
  dom.btnCapturarFoto.disabled = true;
  dom.btnCapturarFoto.textContent = 'Processando Biometria...';
  hideAlert(dom.alertCamera);

  const canvas = document.createElement('canvas');
  canvas.width = dom.cameraFeed.videoWidth || 640;
  canvas.height = dom.cameraFeed.videoHeight || 480;
  const ctx = canvas.getContext('2d');
  ctx.drawImage(dom.cameraFeed, 0, 0, canvas.width, canvas.height);

  const base64Data = canvas.toDataURL('image/jpeg', 0.9);

  try {
    const res = await fetch('/api/v1/totem/reconhecer', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + state.token
      },
      body: JSON.stringify({
        fotoCapturadaBase64: base64Data,
        fotoBase64: base64Data,
        threshold: 0.65
      })
    });

    const data = await res.json();

    if (!res.ok || !data.sucesso) {
      const msg = data.mensagem || 'Biometria facial não confere com o cadastro.';
      showAlert(dom.alertCamera, 'error', msg);
      dom.btnCapturarFoto.disabled = false;
      dom.btnCapturarFoto.textContent = '📸 Tentar Novamente';
      return;
    }

    pararCamera();
    abrirEtapaProntuario();
  } catch (err) {
    showAlert(dom.alertCamera, 'error', 'Erro ao validar biometria: ' + err.message);
    dom.btnCapturarFoto.disabled = false;
    dom.btnCapturarFoto.textContent = '📸 Tentar Novamente';
  }
}

async function abrirEtapaProntuario() {
  mudarEtapa(dom.stepProntuario);
  iniciarTimerInatividade();
  await carregarDadosCompletosProntuario();
}

async function carregarDadosCompletosProntuario() {
  try {
    const resUser = await fetch('/api/v1/usuarios/' + state.usuarioId);
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

    const resPenas = await fetch('/api/v1/penas/usuario/' + state.usuarioId);
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

      const resRegs = await fetch('/api/v1/registros-trabalho/pena/' + penaAtiva.idPena);
      let registros = [];
      if (resRegs.ok) {
        registros = await resRegs.json();
        renderizarTabelaRegistros(registros);
        horasCumpridas = registros.reduce((acc, r) => acc + (r.horasCumpridas || 0), 0);
      }

      try {
        const resResumo = await fetch('/api/v1/registros-trabalho/pena/' + penaAtiva.idPena + '/resumo');
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
