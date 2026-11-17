const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
  isElectron: true,
  platform: process.platform,
  getVersion: () => ipcRenderer.invoke('get-app-version'),
  getConfig: () => ipcRenderer.invoke('get-totem-config'),
  saveConfig: (config) => ipcRenderer.invoke('save-totem-config', config),
  exitKiosk: () => ipcRenderer.send('totem-exit-kiosk'),
  enterKiosk: () => ipcRenderer.send('totem-enter-kiosk'),
  quitApp: () => ipcRenderer.send('app-quit'),
  print: () => ipcRenderer.send('window-print'),
});
