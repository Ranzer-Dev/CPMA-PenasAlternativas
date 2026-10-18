# LLMS.md — CPMA Desktop Delegado (Mesa do Delegado)

> **Documentação Técnica para LLMs e Engenheiros de Software**  
> Este documento resume a arquitetura, regras de negócio, componentes e fluxo de dados do frontend `cpma-desktop-delegado`. Ele permite entender o sistema sem iterar por múltiplos arquivos.

---

## 1. Identificação e Propósito
- **Nome do Projeto**: `cpma-desktop-delegado`
- **Público-Alvo**: Delegado de Polícia, Assistentes Sociais e Administradores da Central de Penas e Medidas Alternativas (CPMA).
- **Finalidade**: Sistema administrativo (desktop) para gestão integral de apenados que cumprem penas e medidas alternativas, controle de horas, cadastro de instituições parceiras conveniadas e emissão de códigos PIN para autoatendimento no Totem.
- **Importante**: Este módulo **NÃO** contém telas de autoatendimento nem biometria facial interativa de totem (essas funções pertencem exclusivamente ao `cpma-desktop-totem`).

---

## 2. Stack Tecnológico e Configuração
| Tecnologia | Versão / Tipo | Função |
| :--- | :--- | :--- |
| **Electron** | ^34.2.0 | Wrapper desktop nativo para Windows (Janela 1280x850, redimensionável) |
| **React** | ^18.3.1 | Biblioteca de interface e estado |
| **TypeScript** | ^5.7.3 | Tipagem estática |
| **Vite** | ^6.1.0 | Bundler e servidor de desenvolvimento (Porta: `3000`, `base: './'`) |
| **Tailwind CSS** | ^4.0.6 | Framework de estilos e design system |
| **Lucide React** | ^0.475.0 | Conjunto de ícones vetoriais modernos |

- **Arquivo de entrada Vite**: `src-web/main.tsx` + `index.html`
- **Entrada Electron**: `electron/main.cjs`
- **Preload Electron**: `electron/preload.cjs`

---

## 3. Arquitetura da Interface (Padrão SaaS)
O aplicativo adota layout estilo SaaS moderno composto por:
1. **`Sidebar.tsx` (Menu Lateral Retrátil)**:
   - **Topo**: Brasão do Estado de São Paulo (`/images/logoSP.png`), sigla CPMA e badge "SaaS".
   - **VISÃO GERAL**: `dashboard` (Painel Geral).
   - **EXECUÇÃO PENAL**: `usuarios` (Apenados), `penas` (Penas Alternativas), `instituicoes` (Instituições Conveniadas), `pontos` (Frequência / Ponto).
   - **AUDITORIA & CONFORMIDADE**: `auditoria` (Trilha de Auditoria).
   - **Rodapé**: Cartão do delegado logado com avatar, indicador de status "Sessão Ativa" e botão de encerramento de sessão (`logout`).
2. **`HeaderSaaS.tsx` (Topbar de Produtividade)**:
   - **Breadcrumbs**: Hierarquia da navegação ativa (`CPMA > [Módulo] > [Subitem]`).
   - **Busca Rápida Global**: Input com atalho para localizar apenado por CPF ou nome em tempo real.
   - **Badge de Conexão**: Status do banco de dados (ex: `PostgreSQL Ativo`).
   - **Atalho de Ação**: Botão `+ Novo Apenado` para criação direta de prontuário.
3. **`App.tsx` (Shell Orquestrador)**:
   - Gerencia a máquina de estados da visão ativa (`viewAtual`), sessão de autenticação (`adminLogado`) e parâmetros de navegação entre prontuários.

---

## 4. Mapa de Componentes e Funcionalidades

### `LoginView.tsx` (Autenticação Institucional)
- **Visual**: Fundo claro com o brasão oficial do Governo do Estado de São Paulo e Secretaria da Administração Penitenciária.
- **Campos**: CPF (com máscara automática `000.000.000-00`) e Senha de acesso com botão de visualização (Eye/EyeOff).
- **Atalho de Teste**: Botão "Preencher dados teste" (CPF `123.456.789-00` / Senha `123456`).
- **Navegação**: Transita para a visualização `RedefinirSenhaView.tsx` em caso de esquecimento.

### `DashboardView.tsx` (Painel Geral)
- **Painéis de Controle (KPIs Solicitados)**:
  1. `Apenados Cadastrados`: Contador com status "Ativos no Sistema".
  2. `Processos / Penas`: Total de processos com status "Em Cumprimento".
  3. `Instituições Parceiras`: Total de instituições com status "Conveniadas".
- **Busca Rápida de Apenado**:
  - Busca por nome, CPF (com/sem pontuação) ou código da pena.
  - Exibe lista suspensa de resultados com avatar, nome, CPF formatado e código do processo.
  - Ao clicar no resultado, dispara `onSelecionarApenado(u)` abrindo o `DetalheApenadoView`.

### `DetalheApenadoView.tsx` (Prontuário Completo do Apenado)
- **Cabeçalho**: Foto 3x4 do apenado, dados cadastrais (CPF, RG, Data de Nascimento, Telefone, Endereço).
- **Resumo da Pena**: Total de horas atribuídas, horas cumpridas, saldo devedor e barra de progresso visual.
- **Tabela de Histórico**: Registros de presença, comparecimentos manuais e faltas justificadas.
- **Geração de PIN do Totem**: Emite o código numérico de 6 dígitos temporário para que o apenado possa fazer autoatendimento no totem externo.

### `CadastroUsuarioView.tsx` (Formulário do Apenado)
- Cadastro e edição de apenados.
- Validação algorítmica de CPF (`limparCPF`, `validarCPF`).
- Captura de foto via webcam ou upload de imagem.

### `CadastroPenaView.tsx`
- Vinculação de processos criminais, varas de origem, número do processo judicial, total de horas fixadas na sentença e prazos legais de cumprimento.

### `CadastroInstituicaoView.tsx`
- Cadastro de escolas, ONGs, hospitais e entidades públicas parceiras conveniadas para acolher os prestadores de serviço à comunidade.

### `CadastroRegistroDeTrabalhoView.tsx`
- Lançamento manual de folhas de ponto, abono de horas, controle de atrasos e atestados médicos.

### `AuditoriaView.tsx`
- Visualização de logs de auditoria e conformidade técnica (LGPD, tentativas de acesso, alterações cadastrais e lançamentos de penas).

---

## 5. Armazenamento de Dados e Comunicação com Backend
- **MockStore (`data/mockStore.ts`)**:
  - Fornece fallback resiliente em memória caso o backend Spring Boot ou PostgreSQL local esteja indisponível durante testes.
  - Mantém tabelas de administradores, apenados, penas, instituições e registros de frequência.
- **APIs REST (`/api/v1/*`)**:
  - `GET /api/v1/usuarios`: Lista todos os apenados.
  - `GET /api/v1/instituicoes`: Lista instituições parceiras.
  - `GET /api/v1/penas`: Lista processos e penas ativas.
  - `POST /api/v1/totem/etapa1/gerar-codigo`: Gera PIN de 6 dígitos para o Totem.

---

## 6. Comandos e Scripts Úteis
```bash
# Desenvolvimento local web (porta 3000)
npm run dev

# Execução no Electron (janela desktop de desenvolvimento)
npm run electron:dev

# Compilação estática do React/Vite
npm run build

# Empacotar executável portátil e instalador NSIS (.exe) na pasta release/
npm run electron:dist
```
