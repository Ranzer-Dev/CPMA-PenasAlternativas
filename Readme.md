# CPMA - Sistema de Gestão de Penas Alternativas

## 📋 Descrição

O **CPMA (Centro de Penas e Medidas Alternativas)** é um sistema desktop desenvolvido em Java com JavaFX para gerenciar e controlar penas alternativas, instituições parceiras e acordos de trabalho. O sistema permite aos administradores cadastrar, consultar e gerenciar informações relacionadas a penas alternativas, incluindo usuários, instituições, disponibilidades e registros de trabalho.

## 🎯 Funcionalidades Principais

### 👤 Gestão de Usuários

- **Cadastro de Usuários**: Sistema completo de cadastro com validações
- **Autenticação**: Login seguro com hash de senhas
- **Recuperação de Senha**: Sistema de redefinição de senha com perguntas secretas
- **Controle de Acesso**: Diferentes níveis de permissão para administradores

### 🏛️ Gestão de Instituições

- **Cadastro de Instituições**: Registro completo de instituições parceiras
- **Tipos de Instituição**: Categorização das instituições
- **Disponibilidades**: Controle de horários e dias disponíveis para trabalho
- **Informações de Contato**: Responsáveis, telefones e endereços

### ⚖️ Gestão de Penas Alternativas

- **Cadastro de Penas**: Registro detalhado de penas alternativas
- **Controle de Tempo**: Acompanhamento de horas semanais e totais
- **Atividades Acordadas**: Descrição das atividades a serem realizadas
- **Vinculação**: Associação entre usuários, penas e instituições

### 📊 Controle de Trabalho

- **Registros de Trabalho**: Acompanhamento das atividades realizadas
- **Acordos de Trabalho**: Contratos entre usuários e instituições
- **Relatórios**: Sistema de busca e consulta de cadastros

### 🔍 Identificação Facial

- **Reconhecimento Facial**: Identificação de penados através de imagens faciais
- **Captura de Imagem**: Suporte para webcam e upload de arquivos
- **Cadastro de Dados Faciais**: Armazenamento de descritores faciais para identificação
- **Busca por Similaridade**: Algoritmo de comparação facial para identificação automática

## 🏗️ Arquitetura do Sistema

### Padrão MVC (Model-View-Controller)

- **Model**: Classes de entidade (Usuario, Pena, Instituicao, etc.)
- **View**: Interfaces FXML com JavaFX
- **Controller**: Lógica de negócio e controle de interface

### Camadas de Acesso a Dados

- **DAO (Data Access Object)**: Padrão para acesso ao banco de dados
- **Database**: Configuração e conexão com SQLite
- **Connection Factory**: Gerenciamento de conexões

## 🛠️ Tecnologias Utilizadas

### Backend

- **Java 17+**: Linguagem principal
- **JavaFX 23**: Framework para interface gráfica
- **Maven**: Gerenciamento de dependências e build

### Banco de Dados

- **SQLite 3**: Banco de dados embarcado
- **JDBC**: Driver SQLite-JDBC para conexão

## 📁 Estrutura do Projeto

```
CPMA-PenasAlternativas/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controller/          # Controladores da aplicação
│   │   │   ├── dao/                 # Data Access Objects
│   │   │   ├── database/            # Configuração do banco
│   │   │   ├── model/               # Modelos de dados
│   │   │   ├── util/                # Utilitários
│   │   │   └── view/                # Views JavaFX
│   │   └── resources/
│   │       ├── view/                # Arquivos FXML
│   │       └── db.properties.example # Configuração do banco
├── script/
│   ├── penas-alternativas.sql      # Script de criação do banco SQLite
│   └── dados-faciais.sql           # Script de criação da tabela de dados faciais
├── pom.xml                         # Configuração Maven
└── README.md                       # Este arquivo
```

## 🚀 Como Executar

### Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- SQLite 3 (opcional, apenas para acesso via linha de comando)

### 1. Configuração do Banco de Dados

O banco de dados SQLite será criado automaticamente quando a aplicação for executada pela primeira vez.
O arquivo `penas_alternativas.db` será criado no diretório raiz do projeto.

**Opção 1: Criação automática (recomendado)**

```bash
# A aplicação criará o banco automaticamente na primeira execução
# Não é necessário executar scripts manualmente
```

**Opção 2: Criação manual (opcional)**

```bash
# Se você tiver SQLite instalado, pode criar o banco manualmente:
sqlite3 penas_alternativas.db < script/penas-alternativas.sql
sqlite3 penas_alternativas.db < script/dados-faciais.sql

# Para verificar as tabelas criadas:
sqlite3 penas_alternativas.db ".tables"

# Para ver a estrutura de uma tabela:
sqlite3 penas_alternativas.db ".schema Usuario"
```

### 2. Configuração da Aplicação

```bash
# Copiar o arquivo de exemplo de configuração (se necessário)
cp src/main/resources/db.properties.example src/main/resources/db.properties

# O arquivo db.properties já está configurado para SQLite
# O caminho do banco pode ser alterado se necessário
```

### 3. Compilação e Execução

```bash
# Compilar o projeto
mvn clean compile

# Executar a aplicação
mvn javafx:run
```

## 🔧 Configurações

### Banco de Dados

- **Tipo**: SQLite 3
- **Arquivo**: `penas_alternativas.db` (criado no diretório raiz do projeto)
- **Driver**: `org.xerial.sqlite-jdbc`
- **URL**: `jdbc:sqlite:penas_alternativas.db`
- **Usuário/Senha**: Não necessário (SQLite não requer autenticação)

### JavaFX

- **Main Class**: view.LoginView
- **Módulos**: javafx.controls, javafx.fxml, java.sql

## 📊 Modelo de Dados

### Entidades Principais

- **Usuario**: Informações pessoais e cadastrais
- **Pena**: Detalhes da pena alternativa
- **Instituicao**: Dados da instituição parceira
- **Administrador**: Usuários do sistema
- **RegistroDeTrabalho**: Controle de atividades
- **AcordoDeTrabalho**: Contratos estabelecidos
- **DadosFaciais**: Informações faciais para identificação biométrica

### Relacionamentos

- Usuário ↔ Pena (1:N)
- Pena ↔ Instituicao (N:1)
- Usuário ↔ RegistroDeTrabalho (1:N)
- Instituicao ↔ DisponibilidadeInstituicao (1:N)
- Usuário ↔ DadosFaciais (1:1)

## 🔐 Segurança

- **Hash de Senhas**: Utilização de algoritmos de hash para senhas
- **Autenticação**: Sistema de login com validação
- **Sessões**: Controle de usuário logado
- **Permissões**: Diferentes níveis de acesso

## 🧪 Testes

Para testar a conexão com o banco de dados:

```bash
# Executar a classe de teste de conexão
mvn exec:java -Dexec.mainClass="database.TestaConexaoBanco"
```

### Migração de SQL Server para SQLite

Este projeto foi migrado de SQL Server para SQLite. As principais mudanças incluem:

- **Driver**: Substituído `mssql-jdbc` por `sqlite-jdbc`
- **Scripts SQL**: Convertidos para sintaxe SQLite (sem IDENTITY, GETDATE(), etc.)
- **Queries**: Atualizadas para usar `LIMIT` ao invés de `TOP`, `strftime()` para funções de data
- **Configuração**: Removida necessidade de usuário/senha e servidor
- **Docker**: Não é mais necessário (docker-compose.yml foi movido para .old)

### Notas sobre SQLite

- O arquivo do banco é criado automaticamente na primeira execução
- O banco é um arquivo único que pode ser facilmente copiado ou movido
- Não há servidor - o banco é acessado diretamente via arquivo
- Backups podem ser feitos simplesmente copiando o arquivo `.db`

### Teste de Identificação Facial

Para testar a funcionalidade de identificação facial:

```bash
# Compilar e executar a aplicação
mvn clean compile
mvn javafx:run

# Navegar para a tela de identificação facial
# Usar a opção "Carregar Arquivo" para testar com imagens
```

## 📝 Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 👥 Autores

- **Equipe CPMA** - Desenvolvimento inicial
- **FATEC** - Instituição de ensino

## 🙏 Agradecimentos

- Professores orientadores
- Equipe de desenvolvimento
- Instituições parceiras

## 📞 Suporte

Para dúvidas ou suporte, entre em contato com a equipe de desenvolvimento ou abra uma issue no repositório.

---

**Desenvolvido com ❤️ para o Centro de Penas e Medidas Alternativas**
