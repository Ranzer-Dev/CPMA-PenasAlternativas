const { app, BrowserWindow, ipcMain, Menu } = require('electron');
const path = require('path');

let mainWindow = null;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 850,
    minWidth: 1024,
    minHeight: 700,
    title: 'CPMA Delegado - Gestão de Penas Alternativas',
    icon: path.join(__dirname, '../dist/images/logoSP.png'),
    backgroundColor: '#f8fafc',
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
    console.error('Falha ao carregar conteúdo da janela:', errorCode, errorDescription);
  });

  if (isDev) {
    mainWindow.loadURL(devServerUrl).catch(() => {
      mainWindow.loadFile(indexPath);
    });
  } else {
    mainWindow.loadFile(indexPath);
  }

  // Create custom application menu
  const template = [
    {
      label: 'Sistema',
      submenu: [
        { label: 'Recarregar', role: 'reload' },
        { label: 'Alternar Tela Cheia', role: 'togglefullscreen' },
        { type: 'separator' },
        { label: 'Sair do CPMA', role: 'quit' },
      ],
    },
    {
      label: 'Exibir',
      submenu: [
        { label: 'Aumentar Zoom', role: 'zoomIn' },
        { label: 'Diminuir Zoom', role: 'zoomOut' },
        { label: 'Resetar Zoom', role: 'resetZoom' },
        { type: 'separator' },
        { label: 'Ferramentas de Desenvolvedor', role: 'toggleDevTools' },
      ],
    },
    {
      label: 'Ajuda',
      submenu: [
        {
          label: 'Sobre o CPMA Desktop',
          click: () => {
            const { dialog } = require('electron');
            dialog.showMessageBox(mainWindow, {
              type: 'info',
              title: 'Sobre o CPMA',
              message: 'CPMA - Central de Penas e Medidas Alternativas',
              detail: 'Versão Desktop Electron 1.0.0\nGoverno do Estado de São Paulo / Município de Votorantim\nParceria com FATEC Sorocaba.',
              buttons: ['OK'],
            });
          },
        },
      ],
    },
  ];

  const menu = Menu.buildFromTemplate(template);
  Menu.setApplicationMenu(menu);

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

// IPC Handlers
ipcMain.handle('get-app-version', () => app.getVersion());

ipcMain.on('window-minimize', () => {
  if (mainWindow) mainWindow.minimize();
});

ipcMain.on('window-maximize', () => {
  if (mainWindow) {
    if (mainWindow.isMaximized()) {
      mainWindow.unmaximize();
    } else {
      mainWindow.maximize();
    }
  }
});

ipcMain.on('window-close', () => {
  if (mainWindow) mainWindow.close();
});

ipcMain.on('window-toggle-fullscreen', () => {
  if (mainWindow) {
    mainWindow.setFullScreen(!mainWindow.isFullScreen());
  }
});

ipcMain.on('window-print', () => {
  if (mainWindow) {
    mainWindow.webContents.print();
  }
});

app.whenReady().then(() => {
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});
