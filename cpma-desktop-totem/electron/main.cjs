const { app, BrowserWindow, ipcMain, Menu, globalShortcut } = require('electron');
const path = require('path');
const fs = require('fs');
const http = require('http');
const { spawn, exec } = require('child_process');

let mainWindow = null;
let pythonProcess = null;

const DEFAULT_CONFIG = {
  serverIp: process.env.CPMA_SERVER_IP || '127.0.0.1',
  serverPort: Number(process.env.CPMA_SERVER_PORT || 8080),
  terminalId: process.env.CPMA_TERMINAL_ID || 'TOTEM-DELEGACIA-01',
  terminalKey: process.env.CPMA_TERMINAL_KEY || 'CPMA-TOTEM-TERMINAL-SECRET-2026-KEY',
};

function getConfigFilePath() {
  return path.join(app.getPath('userData'), 'totem-config.json');
}

function readServerIpFallbackFile() {
  const candidates = [
    path.join(process.cwd(), 'cpma-server-ip.txt'),
    path.join(process.cwd(), '../cpma-server-ip.txt'),
    path.join(__dirname, '../../cpma-server-ip.txt'),
  ];
  for (const file of candidates) {
    try {
      if (fs.existsSync(file)) {
        const content = fs.readFileSync(file, 'utf8').trim();
        if (content) return content;
      }
    } catch {
      // ignora erro de leitura
    }
  }
  return null;
}

function loadTotemConfig() {
  let config = { ...DEFAULT_CONFIG };

  // Se CPMA_API_URL foi passado no ambiente (ex.: http://192.168.0.10:8080/api/v1), extrai host e porta
  if (process.env.CPMA_API_URL) {
    try {
      const parsed = new URL(process.env.CPMA_API_URL);
      if (parsed.hostname) config.serverIp = parsed.hostname;
      if (parsed.port) config.serverPort = Number(parsed.port);
    } catch {
      // ignora URL inválida
    }
  }

  const fallbackIp = readServerIpFallbackFile();
  if (fallbackIp) {
    config.serverIp = fallbackIp;
  }

  try {
    const configPath = getConfigFilePath();
    if (fs.existsSync(configPath)) {
      const saved = JSON.parse(fs.readFileSync(configPath, 'utf8'));
      config = {
        ...config,
        ...saved,
      };
    }
  } catch (err) {
    console.warn('[Totem Config] Aviso ao ler totem-config.json:', err.message);
  }

  return config;
}

function saveTotemConfig(newConfig) {
  const current = loadTotemConfig();
  const merged = {
    ...current,
    ...(newConfig || {}),
    serverIp: String(newConfig?.serverIp || current.serverIp).trim(),
    serverPort: Number(newConfig?.serverPort || current.serverPort || 8080),
    terminalId: String(newConfig?.terminalId || current.terminalId).trim(),
    terminalKey: String(newConfig?.terminalKey || current.terminalKey).trim(),
  };

  try {
    const configPath = getConfigFilePath();
    fs.writeFileSync(configPath, JSON.stringify(merged, null, 2), 'utf8');
  } catch (err) {
    console.error('[Totem Config] Falha ao salvar totem-config.json:', err.message);
  }

  // Também atualiza cpma-server-ip.txt na raiz se possível
  try {
    const rootIpFile = path.join(__dirname, '../../cpma-server-ip.txt');
    fs.writeFileSync(rootIpFile, merged.serverIp, 'utf8');
  } catch {
    // ignora caso diretório seja somente leitura
  }

  return merged;
}

// Checa se o microserviço facial já está ativo na porta 8001
function checkFacialServiceRunning(callback) {
  const req = http.get('http://127.0.0.1:8001/health', (res) => {
    callback(res.statusCode === 200);
  });
  req.on('error', () => callback(false));
  req.setTimeout(800, () => {
    req.abort();
    callback(false);
  });
}

// Localiza o diretório do cpma-facial-service tanto em desenvolvimento quanto empacotado
function resolveFacialServicePath() {
  const candidates = [
    path.join(process.resourcesPath, 'cpma-facial-service'),
    path.join(__dirname, '../../cpma-facial-service'),
    path.join(__dirname, '../../../cpma-facial-service'),
    path.join(process.cwd(), 'cpma-facial-service'),
    path.join(process.cwd(), '../cpma-facial-service'),
  ];
  for (const dir of candidates) {
    if (fs.existsSync(path.join(dir, 'app/main.py'))) {
      return dir;
    }
  }
  return null;
}

// Inicia o microserviço Python silenciosamente em segundo plano (se disponível na máquina do Totem)
function startPythonFacialService() {
  checkFacialServiceRunning((isAlreadyRunning) => {
    if (isAlreadyRunning) {
      console.log('[Sidecar] cpma-facial-service já está ativo na porta 8001.');
      return;
    }

    const serviceDir = resolveFacialServicePath();
    if (!serviceDir) {
      console.warn('[Sidecar] Diretório de cpma-facial-service não localizado. Usando webcam via WebRTC.');
      return;
    }

    console.log('[Sidecar] Disparando cpma-facial-service a partir de:', serviceDir);
    const mainPy = path.join(serviceDir, 'app/main.py');
    const env = {
      ...process.env,
      PYTHONPATH: serviceDir,
    };

    try {
      pythonProcess = spawn('python', [mainPy], {
        cwd: serviceDir,
        env,
        windowsHide: true,
        stdio: ['ignore', 'pipe', 'pipe'],
      });

      pythonProcess.stdout?.on('data', (chunk) => {
        console.log(`[Python Sidecar]: ${chunk.toString().trim()}`);
      });

      pythonProcess.stderr?.on('data', (chunk) => {
        console.warn(`[Python Sidecar Warning]: ${chunk.toString().trim()}`);
      });

      pythonProcess.on('exit', (code, signal) => {
        console.log(`[Sidecar] Processo Python finalizado com código ${code}, sinal ${signal}`);
        pythonProcess = null;
      });
    } catch (err) {
      console.error('[Sidecar] Erro ao iniciar processo Python:', err);
    }
  });
}

// Encerra o processo Python e libera a webcam e a porta 8001
function stopPythonFacialService() {
  if (pythonProcess && pythonProcess.pid) {
    console.log(`[Sidecar] Encerrando processo Python PID: ${pythonProcess.pid}`);
    if (process.platform === 'win32') {
      exec(`taskkill /pid ${pythonProcess.pid} /f /t`, (err) => {
        if (err) console.warn('[Sidecar] Aviso ao finalizar com taskkill:', err.message);
      });
    } else {
      pythonProcess.kill('SIGKILL');
    }
    pythonProcess = null;
  }
}

function createWindow() {
  const isKiosk = process.env.CPMA_KIOSK !== 'false';

  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    fullscreen: isKiosk,
    kiosk: isKiosk,
    autoHideMenuBar: true,
    title: 'CPMA Totem - Autoatendimento Biométrico do Apenado',
    icon: path.join(__dirname, '../public/images/logoSP.png'),
    backgroundColor: '#020617',
    webPreferences: {
      preload: path.join(__dirname, 'preload.cjs'),
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: false,
    },
  });

  const indexPath = path.join(__dirname, '../dist/index.html');
  const devServerUrl = process.env.VITE_DEV_SERVER_URL;
  const isDev = (process.env.NODE_ENV === 'development' || !app.isPackaged) && !!devServerUrl;

  mainWindow.webContents.on('did-fail-load', (event, errorCode, errorDescription) => {
    console.error('Falha ao carregar conteúdo da janela do totem:', errorCode, errorDescription);
  });

  if (isDev) {
    mainWindow.loadURL(devServerUrl).catch(() => {
      mainWindow.loadFile(indexPath);
    });
  } else {
    mainWindow.loadFile(indexPath);
  }

  // Oculta menu no Totem Kiosk
  Menu.setApplicationMenu(null);

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

// IPC Handlers
ipcMain.handle('get-app-version', () => app.getVersion());
ipcMain.handle('get-totem-config', () => loadTotemConfig());
ipcMain.handle('save-totem-config', (_event, newConfig) => saveTotemConfig(newConfig));

ipcMain.on('totem-exit-kiosk', () => {
  if (mainWindow) {
    mainWindow.setKiosk(false);
    mainWindow.setFullScreen(false);
  }
});

ipcMain.on('totem-enter-kiosk', () => {
  if (mainWindow) {
    mainWindow.setFullScreen(true);
    mainWindow.setKiosk(true);
  }
});

ipcMain.on('window-print', () => {
  if (mainWindow) {
    mainWindow.webContents.print({ silent: false, printBackground: true });
  }
});

ipcMain.on('app-quit', () => {
  stopPythonFacialService();
  app.quit();
});

// Ciclo de Vida da Aplicação
app.whenReady().then(() => {
  // Inicia o Sidecar Python automaticamente se presente
  startPythonFacialService();

  createWindow();

  // Atalhos de administração
  globalShortcut.register('CommandOrControl+Shift+Q', () => {
    stopPythonFacialService();
    app.quit();
  });

  globalShortcut.register('F11', () => {
    if (mainWindow) {
      const isFull = mainWindow.isFullScreen();
      mainWindow.setFullScreen(!isFull);
      mainWindow.setKiosk(!isFull);
    }
  });

  globalShortcut.register('F12', () => {
    if (mainWindow) {
      mainWindow.webContents.toggleDevTools();
    }
  });

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on('will-quit', () => {
  stopPythonFacialService();
  globalShortcut.unregisterAll();
});

app.on('window-all-closed', () => {
  stopPythonFacialService();
  if (process.platform !== 'darwin') {
    app.quit();
  }
});
