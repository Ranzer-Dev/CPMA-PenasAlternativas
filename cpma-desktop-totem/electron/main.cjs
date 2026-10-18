const { app, BrowserWindow, ipcMain, Menu, globalShortcut } = require('electron');
const path = require('path');
const fs = require('fs');
const http = require('http');
const { spawn, exec } = require('child_process');

let mainWindow = null;
let pythonProcess = null;

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

// Inicia o microserviço Python silenciosamente em segundo plano
function startPythonFacialService() {
  checkFacialServiceRunning((isAlreadyRunning) => {
    if (isAlreadyRunning) {
      console.log('[Sidecar] cpma-facial-service já está ativo na porta 8001.');
      return;
    }

    const serviceDir = resolveFacialServicePath();
    if (!serviceDir) {
      console.warn('[Sidecar] Diretório de cpma-facial-service não localizado.');
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
    width: 1024,
    height: 768,
    fullscreen: isKiosk,
    kiosk: isKiosk,
    autoHideMenuBar: true,
    title: 'CPMA Totem - Autoatendimento Biométrico do Apenado',
    icon: path.join(__dirname, '../public/images/logoSP.png'),
    backgroundColor: '#0f172a',
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

ipcMain.on('app-quit', () => {
  stopPythonFacialService();
  app.quit();
});

// Ciclo de Vida da Aplicação
app.whenReady().then(() => {
  // Inicia o Sidecar Python automaticamente
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
