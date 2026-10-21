# CPMA - Sistema de Gestão de Penas Alternativas & Biometria Judicial

[![Java Version](https://img.shields.io/badge/Java-24-orange?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Database](https://img.shields.io/badge/Database-SQL_Server_2022-CC292B?style=flat-square&logo=microsoftsqlserver&logoColor=white)](https://www.microsoft.com/sql-server)
[![Vision Engine](https://img.shields.io/badge/Biometria-OpenCV_%7C_JavaCV-5C3EE8?style=flat-square)](https://github.com/bytedeco/javacv)
[![Architecture](https://img.shields.io/badge/Architecture-C4_Model_%7C_DAO_Pattern-blue?style=flat-square)](#-arquitetura-de-software--modelo-c4)
[![Container](https://img.shields.io/badge/Container-Docker_Compose-2496ED?style=flat-square&logo=docker&logoColor=white)](docker-compose.yml)

Sistema corporativo de engenharia de backend para **gestão transacional, governança judicial e fiscalização biométrica presencial** de cidadãos em cumprimento de penas e medidas alternativas. Desenvolvido para garantir rastreabilidade estrita de acordos de trabalho comunitário, auditoria de presença via reconhecimento facial e integridade de dados relacionais em conformidade com as exigências do Poder Judiciário.

---

## 🏛️ Arquitetura de Software — Modelo C4

Para assegurar clareza arquitetural em todos os níveis de abstração técnica, a solução está documentada seguindo a taxonomia completa do **Modelo C4** (Simon Brown), do contexto sistêmico à granularidade de código e modelagem de dados.

```
Nível 1: Contexto Sistêmico  ────────► Atores, Judiciário e Dispositivos Externos
Nível 2: Contêineres         ────────► Runtime Java 24, SQL Server 2022 em Docker, JavaCV
Nível 3: Componentes Backend ────────► Controllers, Engines Biométricas, Guard Clauses e DAOs
Nível 4: Código & Schemas    ────────► Diagrama de Classes e Entidade-Relacionamento (ERD)
```

---

### Nível 1: Diagrama de Contexto (System Context)

O diagrama de contexto estabelece a fronteira do sistema CPMA, destacando os atores humanos (operadores do judiciário e apenados) e os dispositivos físicos periféricos que interagem com o ecossistema.

```mermaid
flowchart TB
    subgraph Atores["Atores do Domínio"]
        Operador["👤 Assistente Social / Oficial Judiciário<br><i>Gerencia penas, acordos de trabalho e fiscalização</i>"]
        Admin["👤 Administrador de TI / Segurança<br><i>Gerencia credenciais, perfis e auditoria</i>"]
        Apenado["👤 Cidadão em Cumprimento de Pena<br><i>Fornece identificação biométrica presencial</i>"]
    end

    subgraph SistemaCPMA["Fronteira do Sistema"]
        CPMA["🏛️ Sistema CPMA<br><b>Gestão de Penas & Biometria</b><br><i>Processa regras de negócio judiciais, valida presenças e audita registros</i>"]
    end

    subgraph Perifericos["Infraestrutura & Dispositivos"]
        Camera["📷 Câmera / Sensor Óptico<br><i>Captura frames de vídeo para extração de biometria facial</i>"]
        Instituicoes["🏢 Instituições Conveniadas<br><i>Entidades que oferecem postos de trabalho comunitário</i>"]
    end

    Operador -->|"Opera cadastros, consulta penas e valida acordos"| CPMA
    Admin -->|"Controla acessos, audita logs e gerencia banco"| CPMA
    Apenado -.->|"Apresenta face no check-in/check-out"| Camera
    Camera -->|"Transmite fluxo de imagem (frames brutos)"| CPMA
    CPMA -->|"Vincula e monitora cumprimento de carga horária"| Instituicoes
```

---

### Nível 2: Diagrama de Contêineres (Containers)

Evidencia a divisão em contêineres lógicos e de infraestrutura: a aplicação desktop em Java 24, a camada nativa de visão computacional (JavaCV/OpenCV) e o banco de dados relacional Microsoft SQL Server 2022 orquestrado via Docker Compose.

```mermaid
flowchart TB
    Operador["👤 Operador Judiciário"]
    DispositivoVideo["📷 Dispositivo de Captura (Webcam / USB)"]

    subgraph ContainerDesktop["Contêiner da Aplicação (Runtime Java 24)"]
        UI["🖥️ Camada de Apresentação (JavaFX 24 / FXML)<br><i>Interfaces gráficas, eventos de tela e visualização biométrica</i>"]
        CoreApp["⚙️ Core Backend Engine (Java 24)<br><i>Regras de negócio, cálculo de penas, validação de CPF e DAOs</i>"]
        OpenCVBridge["👁️ Engine Biometria (JavaCV / OpenCV Platform)<br><i>Processamento de imagem nativo via JNI, Haar Cascades e extração vetorial</i>"]
    end

    subgraph ContainerData["Contêiner de Persistência (Docker)"]
        SQLServer[("🗄️ Microsoft SQL Server 2022<br><b>Porta 1433</b><br><i>Armazena esquemas relacionais, transações ACID e vetores faciais</i>")]
    end

    Operador -->|"Interage via UI"| UI
    UI -->|"Dispara comandos de negócio"| CoreApp
    DispositivoVideo -->|"Frames de vídeo via DirectShow/V4L2"| OpenCVBridge
    OpenCVBridge <-->|"Retorna matrizes de similaridade e predição"| CoreApp
    CoreApp -->|"Consultas e transações ACID via JDBC (TCP 1433)"| SQLServer
```

---

### Nível 3: Diagrama de Componentes (Backend Components)

Demonstra a arquitetura interna do contêiner da aplicação, evidenciando o desacoplamento rigoroso entre **Controladores**, **Serviços de Validação/Guards**, a **Engine Biométrica** e a camada de persistência com o padrão **DAO**.

```mermaid
flowchart TB
    subgraph UI_Events["Camada de Entrada / Eventos"]
        CtrlPena["CadastrarPenaController"]
        CtrlTrabalho["CadastroRegistroDeTrabalhoController"]
        CtrlFacial["IdentificacaoFacialController"]
        CtrlAuth["LoginController"]
    end

    subgraph GuardClauses["Camada de Integridade & Validação (Guard Clauses)"]
        ValidadorCPF["util.ValidadorCPF<br><i>Validação matemática estrita de dígitos verificadores</i>"]
        HashUtil["util.HashUtil<br><i>Hashing criptográfico unidirecional de senhas</i>"]
        CodigoPenaUtil["util.CodigoPenaUtil<br><i>Algoritmo gerador determinístico de código judicial</i>"]
        SessaoUsuario["util.SessaoUsuario<br><i>Gerenciador singleton de estado da sessão autenticada</i>"]
    end

    subgraph VisionEngine["Processamento Biométrico"]
        ReconhecimentoFacial["util.ReconhecimentoFacial<br><i>Classificador Haar Cascade, normalização em escala de cinza e matching</i>"]
    end

    subgraph DAOLayer["Camada de Acesso a Dados (DAO Pattern)"]
        UsuarioDAO["dao.UsuarioDAO"]
        PenaDAO["dao.PenaDAO"]
        RegistroDeTrabalhoDAO["dao.RegistroDeTrabalhoDAO"]
        DadosFaciaisDAO["dao.DadosFaciaisDAO"]
        InstituicaoDAO["dao.InstituicaoDAO"]
    end

    subgraph InfraDB["Infraestrutura de Banco de Dados"]
        ConnectionFactory["database.ConnectionFactory<br><i>Gerenciador de conexões JDBC com retry e leitura de properties</i>"]
        MSSQLDriver["com.microsoft.sqlserver.jdbc"]
    end

    CtrlAuth --> HashUtil
    CtrlAuth --> SessaoUsuario
    CtrlAuth --> UsuarioDAO

    CtrlPena --> CodigoPenaUtil
    CtrlPena --> PenaDAO

    CtrlTrabalho --> ValidadorCPF
    CtrlTrabalho --> RegistroDeTrabalhoDAO

    CtrlFacial --> ReconhecimentoFacial
    ReconhecimentoFacial --> DadosFaciaisDAO

    UsuarioDAO --> ConnectionFactory
    PenaDAO --> ConnectionFactory
    RegistroDeTrabalhoDAO --> ConnectionFactory
    DadosFaciaisDAO --> ConnectionFactory
    InstituicaoDAO --> ConnectionFactory

    ConnectionFactory --> MSSQLDriver
```

---

### Nível 4: Diagrama de Código & Schemas de Dados (Code & Data Model)

O nível mais profundo do C4 detalha o modelo de domínio orientado a objetos e o esquema físico relacional do banco de dados (DDL/ERD).

#### 4.1 Diagrama de Classes de Domínio (Class Diagram)

```mermaid
classDiagram
    class Usuario {
        +int idUsuario
        +String nome
        +String cpf
        +String rg
        +LocalDate dataNascimento
        +String telefone
        +String endereco
        +boolean ativo
        +validarDocumento() boolean
    }

    class Administrador {
        +int idAdmin
        +String login
        +String senhaHash
        +String perfilAcesso
        +autenticar(String senha) boolean
    }

    class Pena {
        +int idPena
        +String codigoPena
        +int idUsuario
        +int idInstituicao
        +int horasTotais
        +int horasCumpridas
        +LocalDate dataInicio
        +LocalDate dataFimPrevista
        +String statusCumprimento
        +calcularProgresso() double
        +isConcluida() boolean
    }

    class Instituicao {
        +int idInstituicao
        +String razaoSocial
        +String cnpj
        +String responsavel
        +String telefone
        +int vagasDisponiveis
    }

    class RegistroDeTrabalho {
        +int idRegistro
        +int idPena
        +LocalDateTime dataHoraEntrada
        +LocalDateTime dataHoraSaida
        +int minutosTrabalhados
        +String observacoes
        +boolean validadoBiometria
    }

    class DadosFaciais {
        +int idDadosFaciais
        +int idUsuario
        +byte[] vetorBiometrico
        +LocalDateTime dataCaptura
        +String versaoAlgoritmo
    }

    Usuario "1" <-- "1" Administrador : estende/associa
    Usuario "1" --> "1..*" Pena : cumpre
    Pena "1..*" --> "1" Instituicao : alocada em
    Pena "1" --> "0..*" RegistroDeTrabalho : audita
    Usuario "1" --> "1" DadosFaciais : possui
```

#### 4.2 Modelo Entidade-Relacionamento (ERD - SQL Server)

Modelagem física implementada no script [penas-alternativas.sql](script/penas-alternativas.sql), garantindo chaves primárias, estrangeiras e integridade referencial:

```mermaid
erDiagram
    USUARIO ||--o{ PENA : "possui"
    USUARIO ||--o| DADOS_FACIAIS : "registra"
    INSTITUICAO ||--o{ PENA : "recebe"
    TIPO_INSTITUICAO ||--o{ INSTITUICAO : "categoriza"
    INSTITUICAO ||--o{ DISPONIBILIDADE_INSTITUICAO : "informa"
    PENA ||--o{ REGISTRO_TRABALHO : "contabiliza"
    ADMINISTRADOR ||--o{ REGISTRO_TRABALHO : "homologa"

    USUARIO {
        int id_usuario PK
        varchar nome
        varchar cpf UK
        varchar rg
        date data_nascimento
        varchar telefone
        varchar endereco
        datetime criado_em
    }

    ADMINISTRADOR {
        int id_admin PK
        varchar login UK
        varchar senha_hash
        varchar nivel_permissao
        datetime criado_em
    }

    TIPO_INSTITUICAO {
        int id_tipo PK
        varchar descricao
    }

    INSTITUICAO {
        int id_instituicao PK
        int id_tipo FK
        varchar razao_social
        varchar cnpj UK
        varchar responsavel
        varchar telefone
        int vagas_totais
    }

    DISPONIBILIDADE_INSTITUICAO {
        int id_disponibilidade PK
        int id_instituicao FK
        varchar dia_semana
        time hora_inicio
        time hora_fim
    }

    PENA {
        int id_pena PK
        varchar codigo_pena UK
        int id_usuario FK
        int id_instituicao FK
        int horas_totais
        int horas_cumpridas
        date data_inicio
        date data_fim_prevista
        varchar status
    }

    REGISTRO_TRABALHO {
        int id_registro PK
        int id_pena FK
        int id_admin_homologador FK
        datetime data_hora_entrada
        datetime data_hora_saida
        int minutos_computados
        bit validado_facial
        text observacao
    }

    DADOS_FACIAIS {
        int id_facial PK
        int id_usuario FK
        varbinary dados_vetor
        datetime criado_em
    }
```

---

## 🛠️ Stack Tecnológica & Racional de Engenharia

| Componente | Tecnologia | Racional Arquitetural |
|---|---|---|
| **Linguagem Core** | **Java 24 (LTS ecosystem)** | Tipagem forte estrita, robustez transacional e gerenciamento determinístico de memória. |
| **Banco de Dados** | **Microsoft SQL Server 2022** | Padrão empresarial para integridade ACID, suporte robusto a transações concorrentes e tipos binários (`varbinary`) para dados biométricos. |
| **Containerização** | **Docker & Docker Compose** | Reprodutibilidade de ambiente local idêntico à produção para o banco de dados relacional. |
| **Visão Computacional** | **JavaCV / OpenCV** | Extração de características faciais de alta velocidade acoplada via pontes nativas C++ JNI. |
| **Padrão de Dados** | **DAO (Data Access Object)** | Isolamento total entre a lógica de negócios e as instruções SQL nativas via JDBC. |

---

## 🚀 Execução Local da Infraestrutura e Aplicação

### 1. Inicialização do Banco de Dados Relacional (Docker)

Suba o container do SQL Server e provisione as tabelas e constraints:

```bash
# 1. Iniciar o container SQL Server 2022
docker-compose up -d

# 2. Copiar o script DDL com o esquema de dados para o container
docker cp ./script/penas-alternativas.sql sqlserver_dev:/tmp/

# 3. Executar o provisionamento com sqlcmd
docker exec -it sqlserver_dev /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U SA -P 'MinhaSenha_forte123' \
  -i /tmp/penas-alternativas.sql -C
```

### 2. Configuração de Credenciais de Banco

```bash
# Copiar o template de propriedades
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

### 3. Build & Execução do Core

```bash
# Compilar todas as classes e validar dependências via Maven
mvn clean compile

# Executar a aplicação
mvn javafx:run
```

---

## 🔒 Segurança, Confiabilidade e Governança de Dados

- **Proteção Criptográfica:** Nenhuma senha trafega ou é persistida em texto plano; todas passam por digest criptográfico via `HashUtil`.
- **Validação na Borda (Guard Clauses):** Todo CPF inserido no sistema é submetido à validação algorítmica de dígitos de controle antes de qualquer chamada ao banco de dados.
- **Rastreabilidade Judicial:** Cada registro de presença possui vínculo explícito com o identificador de homologação e indicação booleana de validação biométrica (`validado_facial`).

---

<div align="center">
  <sub>Documentação arquitetural de backend elaborada com padrões de Engenharia de Software e Modelo C4.</sub>
</div>
