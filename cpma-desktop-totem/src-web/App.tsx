import { useState, useEffect, useCallback } from 'react';
import { TotemConfig, TotemStatusResponse } from './types';
import { getTotemConfig, initTotemConfig, totemApi } from './services/totemApi';
import { TotemKioskView } from './components/TotemKioskView';
import { ConfigServidorModal } from './components/ConfigServidorModal';

export function App() {
  const [config, setConfig] = useState<TotemConfig>(getTotemConfig());
  const [serverStatus, setServerStatus] = useState<TotemStatusResponse | null>(null);
  const [configModalOpen, setConfigModalOpen] = useState<boolean>(false);

  const checkServerConnectivity = useCallback(async (overrideCfg?: TotemConfig) => {
    try {
      const resp = await totemApi.verificarStatusServidor(overrideCfg);
      setServerStatus(resp);
    } catch {
      setServerStatus(null);
    }
  }, []);

  useEffect(() => {
    let mounted = true;
    initTotemConfig().then((loadedCfg) => {
      if (!mounted) return;
      setConfig(loadedCfg);
      checkServerConnectivity(loadedCfg);
    });

    const interval = setInterval(() => {
      checkServerConnectivity();
    }, 15000);

    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, [checkServerConnectivity]);

  // Atalho de teclado Ctrl+Shift+C para abrir modal de configuração de IP do servidor (Operador)
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key.toLowerCase() === 'c') {
        e.preventDefault();
        setConfigModalOpen((prev) => !prev);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  return (
    <>
      <TotemKioskView
        config={config}
        serverStatus={serverStatus}
        onOpenConfig={() => setConfigModalOpen(true)}
        onRefreshStatus={() => checkServerConnectivity(config)}
      />

      <ConfigServidorModal
        isOpen={configModalOpen}
        onClose={() => setConfigModalOpen(false)}
        onSaved={(newConfig, newStatus) => {
          setConfig(newConfig);
          setServerStatus(newStatus);
        }}
      />
    </>
  );
}

export default App;
