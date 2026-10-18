# LLMS.md — CPMA Desktop Totem (Terminal de Autoatendimento do Apenado)

> **Documentação Técnica para LLMs e Engenheiros de Software**  
> Este documento resume a arquitetura, regras de negócio, ciclo de vida do Sidecar Python e fluxo operacional do frontend `cpma-desktop-totem`. Permite entender o sistema sem iterar por múltiplos arquivos.

---

## 1. Identificação e Propósito
- **Nome do Projeto**: `cpma-desktop-totem`
- **Público-Alvo**: Apenados cumprindo penas e medidas alternativas em autoatendimento.
- **Finalidade**: Terminal físico Kiosk autônomo (tela cheia travada sem barras de SO) para que o apenado consulte seu saldo de cumprimento de pena (horas cumpridas, faltantes e percentual) e registre sua frequência através de validação biométrica facial em tempo real.
- **Importante**: Este módulo **NÃO** possui telas administrativas, cadastros ou tabelas de gestão (essas funções pertencem exclusivamente ao `cpma-desktop-delegado`).

---

## 2. Stack Tecnológico e Configuração
| Tecnologia | Versão / Tipo | Função |
| :--- | :--- | :--- |
| **Electron** | ^34.2.0 | Wrapper Kiosk desktop (`fullscreen: true`, `kiosk: true`, `autoHideMenuBar: true`) |
| **React** | ^18.3.1 | Framework de interface reativa |
| **TypeScript** | ^5.7.3 | Tipagem estática |
| **Vite** | ^6.1.0 | Bundler (Porta: `3001`, `base: './'`) |
| **Tailwind CSS** | ^4.0.6 | Framework visual com tema dark slate de alto contraste |
| **Lucide React** | ^0.475.0 | Ícones de interface |

- **Arquivo de entrada Vite**: `src-web/main.tsx` + `index.html`
- **Entrada Electron**: `electron/main.cjs`
- **Preload Electron**: `electron/preload.cjs`

---

## 3. Arquitetura do Sidecar Python (`cpma-facial-service`)
O Electron do Totem atua como gerenciador do microserviço Python local (`cpma-facial-service`), eliminando a necessidade de iniciar o Python manualmente:

1. **Inicialização Automática (`startPythonFacialService` em `main.cjs`)**:
   - No evento `app.whenReady()`, o Electron testa se a porta `8001` responde a `GET http://127.0.0.1:8001/health`.
   - Se estiver inativo, localiza a pasta `cpma-facial-service` (tanto em dev quanto nos `extraResources` da pasta instalada) e dispara:
     ```bash
     python app/main.py
     ```
     em modo oculto (`windowsHide: true`, sem abrir terminal preto).
2. **Encerramento Automático (`stopPythonFacialService` em `main.cjs`)**:
   - Ao fechar o Totem (`will-quit` ou `window-all-closed`), executa:
     ```bash
     taskkill /pid <PID> /f /t
     ```
     Isso garante que a webcam e a porta `8001` sejam liberadas sem processos órfãos.
3. **Atalhos de Emergência do Operador**:
   - `Ctrl+Shift+Q`: Encerra o Totem e o processo Python imediatamente.
   - `F11`: Alterna entre tela cheia Kiosk e janela normal.
   - `F12`: Abre o Console do Desenvolvedor (DevTools) para diagnóstico.

---

## 4. O Fluxo de 3 Etapas do Apenado (`TotemKioskView.tsx`)

O aplicativo é estruturado em uma máquina de estados com 3 etapas lineares e seguras:

```text
[ Etapa 1: PIN de 6 Dígitos ]
          │ (Valida no Backend)
          ▼
[ Etapa 2: Biometria Facial com Anti-Spoofing ]
          │ (Stream MJPEG + Comparação com Foto Cadastrada)
          ▼
[ Etapa 3: Resumo de Horas & Comprovante de Presença ]
          │
          └── Auto-reset regressivo (15s) ──> [ Retorna à Etapa 1 ]
```

### Etapa 1: Digitação do PIN (Identificação)
- **Interface**: Teclado virtual numérico touchscreen (dígitos `0` a `9`, botão `LIMPAR`, botão `CORRIGIR`).
- **Segurança**: Exibe 6 slots com máscara de segurança (`•`).
- **Validação**: Envia `POST /api/v1/totem/etapa1/validar-codigo` com `{ codigo: pin }`.
- **Resultado**: Retorna o ID do apenado, nome, código da pena e a foto de referência cadastrada.
- **Atalho de Teste**: Botão "Preencher código de teste (123456)" para demonstração rápida sem backend.

### Etapa 2: Validação Biométrica Facial
- **Câmera ao Vivo**:
  - Se o `cpma-facial-service` estiver ativo, consome o stream MJPEG direto em `http://localhost:8001/camera/stream`.
  - O vídeo exibe retângulos de foco dinâmicos: verde para rosto real autenticado e vermelho com aviso se detectar tentativa de foto/celular (Anti-Spoofing).
  - Possui fallback automático para WebRTC local (`navigator.mediaDevices.getUserMedia`) caso o microserviço esteja iniciando.
- **Ação "Autenticar e Consultar"**:
  - Captura o snapshot (`/camera/capture`).
  - Executa a comparação facial no microserviço (`POST /compare`).
  - Registra a presença no backend (`POST /api/v1/totem/etapa2/validar-biometria`).

### Etapa 3: Resumo de Cumprimento da Pena
- **Confirmação Visual**: Ícone de check verde e mensagem de presença autenticada.
- **Identificação do Apenado**: Nome completo, CPF mascarado e código do processo.
- **Métricas da Pena**:
  - Barra de progresso visual de preenchimento percentual.
  - Horas Cumpridas (verde).
  - Horas Faltantes (âmbar).
  - Total de Horas da Pena (azul).
- **Auto-Reset (LGPD / Privacidade)**:
  - Temporizador regressivo de **15 segundos** na tela.
  - Ao zerar (ou ao clicar em "Finalizar Consulta"), limpa todos os dados sensíveis da memória e retorna à Etapa 1 para o próximo apenado.

---

## 5. Serviços e APIs Integradas
- **`services/facialService.ts`**:
  - `checkHealth()`: Verifica `http://localhost:8001/health`.
  - `getStreamUrl()`: Retorna URL do feed MJPEG com bounding box (`http://localhost:8001/camera/stream`).
  - `captureSnapshot()`: Captura frame validado com anti-spoofing (`http://localhost:8001/camera/capture`).
  - `compareFaces(imgA, imgB, threshold)`: Executa comparação via cosseno/distância euclidiana com threshold padrão de 0.6.
- **APIs do Backend Central (porta `8080`)**:
  - `POST /api/v1/totem/etapa1/validar-codigo`: Valida se o PIN está ativo.
  - `POST /api/v1/totem/etapa2/validar-biometria`: Grava a presença no banco de dados.

---

## 6. Comandos e Scripts Úteis
```bash
# Desenvolvimento local web (porta 3001)
npm run dev

# Execução no Electron em modo Kiosk de desenvolvimento
npm run electron:dev

# Compilação estática do React/Vite (base relativa './')
npm run build

# Empacotar executável do Totem e instalador com Sidecar (.exe) na pasta release/
npm run electron:dist
```
