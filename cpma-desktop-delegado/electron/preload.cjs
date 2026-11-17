const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
  isElectron: true,
  platform: process.platform,
  getVersion: () => ipcRenderer.invoke('get-app-version'),
  getApiBaseUrl: () => ipcRenderer.invoke('get-api-base-url'),
  setApiBaseUrl: (url) => ipcRenderer.invoke('set-api-base-url', url),
  minimize: () => ipcRenderer.send('window-minimize'),
  maximize: () => ipcRenderer.send('window-maximize'),
  close: () => ipcRenderer.send('window-close'),
  toggleFullscreen: () => ipcRenderer.send('window-toggle-fullscreen'),
  print: () => ipcRenderer.send('window-print'),
});
