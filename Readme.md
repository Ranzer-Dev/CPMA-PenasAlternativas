# CPMA - Sistema de Gestão de Penas Alternativas e Totem Biométrico

## 📋 Descrição

O **CPMA (Centro de Penas e Medidas Alternativas)** é um ecossistema distribuído para gestão, acompanhamento e validação biométrica de penas alternativas, instituições parceiras e registros de trabalho.

## 🏗️ Arquitetura do Sistema

O projeto é composto por 4 módulos principais e banco de dados **PostgreSQL**:

```
CPMA-PenasAlternativas/
├── cpma-backend/             # API REST central em Java 17 + Spring Boot 3 + JPA/Hibernate (PostgreSQL)
├── cpma-facial-service/      # Microserviço de Biometria Facial em Python (FastAPI + OpenCV + YuNet + FaceNet ONNX)
├── cpma-desktop-delegado/    # Painel Administrativo Desktop (Electron + React + TypeScript + Tailwind CSS)
├── cpma-desktop-totem/       # Terminal Kiosk de Autoatendimento do Apenado (Electron + React + Sidecar Biométrico)
├── scripts/                  # Scripts de inicialização e descoberta automática de rede LAN
├── docker-compose.yml        # Container oficial do PostgreSQL 16 (cpma-postgres)
├── start_admin.bat           # Inicializador completo do PC do Delegado (Servidor + Painel)
├── start_totem.bat           # Inicializador do Terminal Totem Kiosk
└── test_all.bat              # Suíte completa de testes automatizados e validação de builds
```

## 🛠️ Tecnologias Utilizadas

- **Banco de Dados**: PostgreSQL 16 (exclusivo)
- **Backend (`cpma-backend`)**: Java 17, Spring Boot 3.3.4, Spring Data JPA, PostgreSQL JDBC, JWT (`jjwt`)
- **Serviço Biométrico (`cpma-facial-service`)**: Python 3.10+, FastAPI, Uvicorn, OpenCV, ONNX Runtime (YuNet + FaceNet)
- **Aplicações Desktop (`cpma-desktop-delegado` e `cpma-desktop-totem`)**: Electron 34, React 18, TypeScript, Vite 6, Tailwind CSS 4

## 🚀 Como Executar

### 1. Banco de Dados PostgreSQL

Inicie o PostgreSQL localmente na porta `5432` (banco `cpma`) ou via Docker:

```bash
docker compose up -d
```

Variáveis de ambiente suportadas pelo `cpma-backend` (com valores padrão):
- `CPMA_DB_URL` (`jdbc:postgresql://127.0.0.1:5432/cpma`)
- `CPMA_DB_USER` (`postgres`)
- `CPMA_DB_PASS` (`postgres`)

### 2. Iniciar o PC do Delegado (Servidor + Painel Administrativo)

```bat
.\start_admin.bat
```
Ou via npm:
```bash
npm run start:admin
```

### 3. Iniciar o Terminal Totem (Autoatendimento do Apenado)

```bat
.\start_totem.bat
```
Ou via npm:
```bash
npm run start:totem
```

## 🧪 Testes e Validação de Build

Para rodar a bateria completa de testes (`cpma-backend`, `cpma-facial-service`) e validar os builds de `cpma-desktop-delegado` e `cpma-desktop-totem`:

```bat
.\test_all.bat
```
