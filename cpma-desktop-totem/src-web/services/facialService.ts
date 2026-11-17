const LOCAL_FACIAL_SERVICE_URL = 'http://127.0.0.1:8001';

export interface LocalCaptureResponse {
  success: boolean;
  frame_base64?: string;
  faces_detected?: number;
  message?: string;
}

export const facialService = {
  /**
   * Verifica se há um cpma-facial-service local rodando na máquina do Totem (porta 8001).
   * Caso não haja, o Totem utiliza automaticamente a webcam via navegador/Electron (WebRTC getUserMedia).
   */
  checkLocalSidecarHealth: async (): Promise<boolean> => {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 1200);
    try {
      const res = await fetch(`${LOCAL_FACIAL_SERVICE_URL}/health`, {
        method: 'GET',
        signal: controller.signal,
      });
      return res.ok;
    } catch {
      return false;
    } finally {
      clearTimeout(timer);
    }
  },

  getLocalMjpegStreamUrl: (cameraIndex = 0): string =>
    `${LOCAL_FACIAL_SERVICE_URL}/camera/stream?camera_index=${cameraIndex}&t=${Date.now()}`,

  captureFromLocalSidecar: async (cameraIndex = 0): Promise<string> => {
    const res = await fetch(
      `${LOCAL_FACIAL_SERVICE_URL}/camera/capture?camera_index=${cameraIndex}`,
      { method: 'POST' },
    );
    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}));
      throw new Error(
        errBody.detail || errBody.message || 'Falha ao capturar frame da câmera no serviço local.',
      );
    }
    const data: LocalCaptureResponse = await res.json();
    if (!data.frame_base64) {
      throw new Error('Imagem vazia retornada pela câmera.');
    }
    return data.frame_base64;
  },
};
