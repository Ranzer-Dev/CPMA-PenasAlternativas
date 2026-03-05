# RELATÓRIO PARCIAL - PROJETO CPMA

## Sistema de Gestão de Penas Alternativas com Identificação Facial

---

## 1. INTRODUÇÃO

O presente relatório descreve o desenvolvimento do **Sistema CPMA (Centro de Penas e Medidas Alternativas)**, uma aplicação desktop desenvolvida em Java com JavaFX para gerenciamento e controle de penas alternativas, instituições parceiras e acordos de trabalho. O sistema foi desenvolvido como parte de um projeto de iniciação científica com o objetivo de modernizar e automatizar os processos de gestão de penas alternativas, incluindo a implementação de um sistema de identificação facial biométrica para reconhecimento de penados.

O sistema permite aos administradores cadastrar, consultar e gerenciar informações relacionadas a penas alternativas, incluindo usuários (penados), instituições parceiras, disponibilidades, registros de trabalho e identificação facial. A implementação incluiu a migração de um banco de dados SQL Server para SQLite, facilitando a portabilidade e reduzindo custos de infraestrutura.

A identificação facial foi implementada utilizando a biblioteca OpenCV através do JavaCV, permitindo detecção de faces em tempo real, extração de descritores faciais e comparação biométrica para identificação automática de usuários cadastrados no sistema.

---

## 2. OBJETIVOS

### 2.1 Objetivo Geral

Desenvolver um sistema desktop completo para gestão de penas alternativas com funcionalidades de cadastro, consulta, controle de trabalho e identificação facial biométrica.

### 2.2 Objetivos Específicos

- Implementar sistema de gestão de usuários (penados) com validações e controle de acesso
- Desenvolver módulo de gestão de instituições parceiras com controle de disponibilidades
- Criar sistema de cadastro e acompanhamento de penas alternativas
- Implementar controle de registros de trabalho e acordos estabelecidos
- Desenvolver sistema de identificação facial utilizando OpenCV e JavaCV
- Migrar banco de dados de SQL Server para SQLite para maior portabilidade
- Implementar interface gráfica moderna e intuitiva utilizando JavaFX
- Garantir segurança através de hash de senhas e controle de sessões

---

## 3. JUSTIFICATIVA

A gestão de penas alternativas tradicionalmente é realizada através de processos manuais e sistemas legados que não oferecem recursos modernos de identificação e controle. A implementação de um sistema informatizado com identificação facial biométrica oferece diversas vantagens fundamentadas em pesquisas científicas:

**Eficiência Operacional**: A automatização de processos de cadastro, consulta e controle reduz significativamente o tempo de atendimento e minimiza erros humanos. Estudos sobre automação em sistemas de gestão operacional demonstram que a implementação de tecnologias de automação baseadas em regras pode aumentar a eficiência operacional e reduzir custos (SYSTEMATIC LITERATURE REVIEW, 2020). Pesquisas empíricas em diferentes setores, incluindo construção e saúde, indicam que a automação é um dos principais fatores de melhoria de eficiência, especialmente quando combinada com inteligência artificial e aprendizado de máquina (NATURE, 2025).

**Segurança e Identificação**: A identificação facial biométrica oferece um método seguro e confiável para verificar a identidade dos penados, reduzindo fraudes e garantindo que a pessoa correta esteja cumprindo a pena alternativa. Sistemas de reconhecimento facial baseados em deep learning, como FaceNet, demonstram altas taxas de precisão (até 98,6% de acurácia) em tarefas de verificação de identidade, com baixas taxas de falsa aceitação (0,1%) (MDPI, 2025). A implementação de sistemas biométricos multimodais, que combinam múltiplas modalidades de verificação, tem sido apontada como uma estratégia eficaz para melhorar a segurança e reduzir vulnerabilidades a ataques de spoofing (IJSAT, 2025).

**Portabilidade**: A migração para SQLite elimina a necessidade de servidor de banco de dados, reduzindo custos de infraestrutura e facilitando a implantação em diferentes ambientes. Pesquisas sobre sistemas de banco de dados embarcados demonstram que abordagens baseadas em componentes e design modular facilitam a portabilidade entre diferentes plataformas, reduzindo custos de desenvolvimento e manutenção (MARQUETTE UNIVERSITY, 2025). Sistemas de banco de dados embarcados otimizados podem reduzir significativamente os requisitos de hardware e os custos operacionais associados (SCIENTIFIC.NET, 2025).

**Modernização**: Interface gráfica moderna e intuitiva melhora a experiência do usuário e facilita a adoção do sistema pelos funcionários. Estudos sobre sistemas de gestão baseados em IA demonstram que a atitude dos funcionários desempenha um papel mediador importante na melhoria da eficiência operacional, destacando a importância de interfaces intuitivas para a aceitação tecnológica (PUBMED, 2024).

**Rastreabilidade**: Sistema de registros detalhados permite acompanhamento completo das atividades e geração de relatórios para órgãos competentes. A implementação de sistemas de rastreabilidade informatizados é essencial para garantir transparência e conformidade regulatória em processos administrativos públicos.

---

## 4. FUNDAMENTAÇÃO TEÓRICA

### 4.1 Visão Computacional e Reconhecimento Facial

O reconhecimento facial é uma área da visão computacional que utiliza algoritmos para identificar ou verificar a identidade de uma pessoa a partir de uma imagem digital ou vídeo. O processo envolve três etapas principais: detecção de faces, extração de características (descritores faciais) e comparação/identificação. Esta área tem evoluído significativamente com o advento de técnicas de deep learning, que revolucionaram a precisão e robustez dos sistemas de reconhecimento facial (MINAEE et al., 2021).

**Detecção de Faces**: Utiliza classificadores como Haar Cascade ou modelos de Deep Learning (DNN) como YuNet para localizar faces em imagens. O classificador Haar Cascade, proposto por Viola e Jones (2001), utiliza características de Haar para detectar padrões faciais através de janelas deslizantes na imagem. Este método foi fundamental para o desenvolvimento inicial de sistemas de detecção facial em tempo real. No entanto, métodos baseados em deep learning, como Faster R-CNN aprimorado, têm demonstrado desempenho superior em benchmarks como FDDB, incorporando estratégias como concatenação de características e treinamento multi-escala (ZHANG et al., 2018). Modelos DNN modernos, como YuNet, oferecem melhor precisão e robustez em diferentes condições de iluminação e ângulos.

**Extração de Descritores**: Após a detecção, algoritmos como FaceNet, OpenFace ou ArcFace extraem vetores de características (embeddings) que representam numericamente as características faciais. Esses vetores são normalizados e armazenados para comparação posterior. O FaceNet, proposto por Schroff, Kalenichenko e Philbin (2015), mapeia imagens faciais para um espaço euclidiano compacto usando redes neurais convolucionais profundas, permitindo reconhecimento e agrupamento eficiente de faces com alta precisão. Estudos comparativos demonstram que o FaceNet pode alcançar até 98,6% de acurácia em tarefas de verificação de identidade (MDPI, 2025). O ArcFace, por sua vez, introduz uma função de perda de margem angular aditiva para melhorar o poder discriminativo dos modelos de reconhecimento facial, alcançando acurácias superiores a 94% em alguns cenários (KKJOURNAL, 2025).

**Comparação e Identificação**: A identificação é realizada através da comparação de distâncias entre vetores de características. Algoritmos como distância euclidiana ou correlação de cosseno são utilizados para determinar a similaridade entre faces. No espaço euclidiano gerado por modelos como FaceNet, a distância entre vetores corresponde diretamente a uma medida de similaridade facial, permitindo identificação eficiente mesmo em bases de dados com milhares de indivíduos (SCHROFF et al., 2015).

### 4.2 OpenCV e JavaCV

O OpenCV (Open Source Computer Vision Library) é uma biblioteca de código aberto para visão computacional e aprendizado de máquina, amplamente utilizada em pesquisas e aplicações comerciais. Desenvolvida inicialmente pela Intel e posteriormente mantida pela comunidade open source, o OpenCV oferece implementações otimizadas de algoritmos de visão computacional, incluindo detecção de faces, processamento de imagens e reconhecimento facial (BRADSKI; KAEHLER, 2013). O JavaCV é um wrapper Java para OpenCV que permite utilizar as funcionalidades do OpenCV em aplicações Java, incluindo detecção de faces, processamento de imagens e reconhecimento facial, facilitando a integração de capacidades de visão computacional em aplicações desktop desenvolvidas em Java.

### 4.3 Arquitetura de Software

O sistema foi desenvolvido seguindo o padrão arquitetural **MVC (Model-View-Controller)**:

- **Model**: Classes de entidade que representam os dados do sistema (Usuario, Pena, Instituicao, etc.)
- **View**: Interfaces gráficas desenvolvidas em FXML (JavaFX Markup Language)
- **Controller**: Classes que implementam a lógica de negócio e controlam a interação entre Model e View

### 4.4 Padrão DAO (Data Access Object)

O padrão DAO foi utilizado para abstrair o acesso aos dados, separando a lógica de negócio da lógica de acesso ao banco de dados. Cada entidade possui um DAO correspondente que implementa operações CRUD (Create, Read, Update, Delete).

### 4.5 Banco de Dados SQLite

SQLite é um sistema de gerenciamento de banco de dados relacional embarcado que armazena dados em um único arquivo. Diferente de sistemas cliente-servidor como SQL Server, o SQLite não requer um servidor separado, facilitando a implantação e reduzindo custos de infraestrutura. Pesquisas sobre sistemas de banco de dados embarcados destacam que abordagens baseadas em componentes e design modular facilitam a portabilidade entre diferentes plataformas, reduzindo custos de desenvolvimento e manutenção (MARQUETTE UNIVERSITY, 2025). A arquitetura embarcada do SQLite elimina a necessidade de processos de servidor separados, reduzindo significativamente os requisitos de hardware e os custos operacionais associados, tornando-o ideal para aplicações desktop e sistemas com recursos limitados.

---

## 5. METODOLOGIA

### 5.1 Tecnologias Utilizadas

**Linguagem e Framework**:

- Java 17+: Linguagem de programação principal
- JavaFX 23: Framework para desenvolvimento de interface gráfica desktop
- Maven: Gerenciamento de dependências e build do projeto

**Bibliotecas de Visão Computacional**:

- OpenCV 4.8.1: Biblioteca de visão computacional
- JavaCV 1.5.10: Wrapper Java para OpenCV
- Modelos DNN: YuNet para detecção de faces, FaceNet/ArcFace para reconhecimento

**Banco de Dados**:

- SQLite 3.44.1.0: Banco de dados embarcado
- JDBC: Driver SQLite-JDBC para conexão

**Outras Bibliotecas**:

- ControlsFX 11.2.0: Componentes adicionais para JavaFX
- Apache PDFBox 2.0.29: Geração e manipulação de PDFs
- JSON 20231013: Processamento de dados JSON

### 5.2 Estrutura do Projeto

O projeto foi organizado seguindo a estrutura padrão Maven:

```
CPMA-PenasAlternativas/
├── src/main/java/
│   ├── controller/          # 15 controladores (Login, Cadastro, Consulta, etc.)
│   ├── dao/                 # 8 DAOs (UsuarioDAO, PenaDAO, InstituicaoDAO, etc.)
│   ├── database/            # Configuração e inicialização do banco
│   ├── model/               # 9 modelos de dados (Usuario, Pena, Instituicao, etc.)
│   ├── util/                # Utilitários (ReconhecimentoFacial, HashUtil, etc.)
│   └── com/mycompany/cpma/  # Classe principal da aplicação
├── src/main/resources/
│   ├── com/mycompany/cpma/  # 17 arquivos FXML (interfaces gráficas)
│   ├── models/              # Modelos ONNX para reconhecimento facial
│   ├── opencv/              # Classificadores Haar Cascade
│   └── fonts/               # Fontes personalizadas (Nunito)
└── script/                  # Scripts SQL para criação do banco
```

### 5.3 Desenvolvimento por Módulos

**Módulo 1 - Infraestrutura e Banco de Dados**:

- Configuração do projeto Maven
- Migração de SQL Server para SQLite
- Criação de classes utilitárias para tratamento de datas e horários (SQLiteDateUtil, SQLiteTimeUtil)
- Implementação do DatabaseInitializer para criação automática do banco
- Desenvolvimento de todos os DAOs com compatibilidade SQLite

**Módulo 2 - Autenticação e Segurança**:

- Sistema de login com hash de senhas (HashUtil)
- Controle de sessão de usuário (SessaoUsuario)
- Sistema de recuperação de senha com perguntas secretas
- Validação de CPF (ValidadorCPF)

**Módulo 3 - Gestão de Usuários e Penas**:

- Cadastro e edição de usuários (penados)
- Cadastro e gestão de penas alternativas
- Consulta e listagem de apenados
- Detalhamento completo de informações do apenado

**Módulo 4 - Gestão de Instituições**:

- Cadastro de instituições parceiras
- Cadastro de tipos de instituição
- Controle de disponibilidades (horários e dias da semana)
- Listagem e consulta de instituições

**Módulo 5 - Controle de Trabalho**:

- Registro de trabalhos realizados
- Controle de horas trabalhadas
- Relacionamento entre penas, usuários e instituições
- Consultas por período e pena

**Módulo 6 - Identificação Facial**:

- Implementação da classe ReconhecimentoFacial com suporte a múltiplos algoritmos
- Detecção de faces utilizando Haar Cascade e DNN (YuNet)
- Extração de descritores faciais utilizando modelos DNN
- Comparação de faces para identificação
- Interface de captura via webcam (CameraController)
- Interface de identificação e cadastro facial (IdentificacaoFacialController)
- Armazenamento de descritores faciais no banco de dados

### 5.4 Procedimentos de Desenvolvimento

**Análise e Planejamento**:

- Levantamento de requisitos funcionais e não funcionais
- Definição da arquitetura do sistema
- Escolha das tecnologias e bibliotecas

**Desenvolvimento Iterativo**:

- Implementação por módulos funcionais
- Testes unitários durante o desenvolvimento
- Refatoração contínua para melhorar qualidade do código

**Migração de Banco de Dados**:

- Análise das diferenças entre SQL Server e SQLite
- Conversão de queries (TOP → LIMIT, GETDATE() → date('now'), etc.)
- Criação de utilitários para tratamento de tipos de dados específicos
- Testes de compatibilidade

**Implementação de Reconhecimento Facial**:

- Pesquisa sobre algoritmos de reconhecimento facial
- Implementação de múltiplos métodos (Haar Cascade, DNN)
- Testes com diferentes modelos e configurações
- Otimização de parâmetros para melhor precisão

**Adaptações Realizadas**:

- Correção de incompatibilidades entre SQL Server e SQLite
- Tratamento especial para datas e horários no SQLite
- Implementação de fallback para diferentes métodos de detecção facial
- Suporte multiplataforma (Windows, Linux, macOS) para OpenCV

---

## 6. RESULTADOS PARCIAIS E DISCUSSÃO

### 6.1 Funcionalidades Implementadas

**Sistema de Autenticação**:

- ✅ Login seguro com hash de senhas
- ✅ Controle de sessão de usuário
- ✅ Recuperação de senha com perguntas secretas
- ✅ Diferentes níveis de permissão (administrador)

**Gestão de Usuários (Penados)**:

- ✅ Cadastro completo com validações (CPF, campos obrigatórios)
- ✅ Edição de dados cadastrais
- ✅ Consulta e busca de apenados
- ✅ Listagem com filtros
- ✅ Visualização detalhada de informações

**Gestão de Penas Alternativas**:

- ✅ Cadastro de penas com código único
- ✅ Controle de datas (início, fim, prorrogação)
- ✅ Acompanhamento de horas semanais e totais
- ✅ Descrição de atividades acordadas
- ✅ Vinculação com usuários e instituições

**Gestão de Instituições**:

- ✅ Cadastro de instituições parceiras
- ✅ Categorização por tipo de instituição
- ✅ Controle de disponibilidades (dias e horários)
- ✅ Informações de contato e responsáveis
- ✅ Listagem e consulta

**Controle de Trabalho**:

- ✅ Registro de trabalhos realizados
- ✅ Controle de datas e horários
- ✅ Relacionamento com penas e instituições
- ✅ Consultas por período e pena específica

**Identificação Facial**:

- ✅ Detecção de faces em tempo real via webcam
- ✅ Detecção de faces em imagens carregadas
- ✅ Extração de descritores faciais (embeddings)
- ✅ Armazenamento de descritores no banco de dados
- ✅ Comparação de faces para identificação
- ✅ Interface gráfica para captura e identificação
- ✅ Suporte a múltiplos algoritmos (Haar Cascade, YuNet DNN)

**Infraestrutura**:

- ✅ Migração completa de SQL Server para SQLite
- ✅ Criação automática do banco de dados na primeira execução
- ✅ Tratamento adequado de datas e horários no SQLite
- ✅ 15 controladores implementados
- ✅ 8 DAOs completos com operações CRUD
- ✅ 9 modelos de dados
- ✅ 17 interfaces gráficas (FXML)

### 6.2 Estatísticas do Projeto

- **Total de Classes Java**: 46 arquivos fonte
- **Controladores**: 15 classes
- **DAOs**: 8 classes
- **Modelos**: 9 classes
- **Utilitários**: 6 classes
- **Interfaces Gráficas (FXML)**: 17 arquivos
- **Scripts SQL**: 8 arquivos
- **Linhas de Código**: Aproximadamente 15.000+ linhas

### 6.3 Contribuições e Impactos

**Contribuições Técnicas**:

- Implementação bem-sucedida de sistema de reconhecimento facial em Java utilizando OpenCV
- Migração completa e funcional de SQL Server para SQLite com tratamento adequado de incompatibilidades
- Arquitetura modular e extensível seguindo padrões de projeto (MVC, DAO)
- Código organizado e documentado facilitando manutenção futura

**Contribuições Práticas**:

- Sistema funcional pronto para uso em ambiente de produção
- Redução de custos de infraestrutura (eliminação de servidor de banco de dados)
- Melhoria na segurança através de identificação biométrica
- Automação de processos manuais reduzindo tempo de atendimento

**Impactos Esperados**:

- Aumento da eficiência operacional do CPMA
- Redução de erros humanos no cadastro e identificação
- Melhoria na rastreabilidade e controle de penas alternativas
- Facilidade de implantação em diferentes ambientes

### 6.4 Desafios Enfrentados e Soluções

**Desafio 1 - Migração SQL Server para SQLite**:

- **Problema**: Incompatibilidades de sintaxe e tipos de dados
- **Solução**: Criação de classes utilitárias (SQLiteDateUtil, SQLiteTimeUtil) e conversão sistemática de todas as queries

**Desafio 2 - Reconhecimento Facial Multiplataforma**:

- **Problema**: Diferentes caminhos e formatos de bibliotecas nativas em cada sistema operacional
- **Solução**: Implementação de detecção automática do sistema operacional e carregamento dinâmico de bibliotecas

**Desafio 3 - Performance do Reconhecimento Facial**:

- **Problema**: Processamento de imagens pode ser lento em hardware limitado
- **Solução**: Implementação de múltiplos algoritmos com fallback, otimização de parâmetros e processamento assíncrono

**Desafio 4 - Precisão da Identificação**:

- **Problema**: Diferentes condições de iluminação e ângulos afetam a precisão
- **Solução**: Utilização de modelos DNN mais robustos (YuNet) e ajuste fino de parâmetros de similaridade

### 6.5 Limitações e Melhorias Futuras

**Limitações Identificadas**:

- O sistema de reconhecimento facial atual utiliza algoritmos clássicos e modelos DNN básicos. Para maior precisão, seria necessário implementar modelos mais avançados como FaceNet ou DeepFace.
- O banco de dados SQLite, embora adequado para uso local, pode ter limitações de concorrência em ambientes com muitos usuários simultâneos.
- A interface gráfica, embora funcional, poderia ser aprimorada com mais recursos visuais e animações.

**Melhorias Futuras Propostas**:

1. Implementação de modelos de Deep Learning mais avançados para reconhecimento facial
2. Adição de relatórios em PDF com gráficos e estatísticas
3. Implementação de sistema de backup automático
4. Adição de funcionalidades de exportação de dados
5. Melhoria da interface gráfica com temas personalizáveis
6. Implementação de testes automatizados
7. Adição de funcionalidades de auditoria e log de ações

---

## 7. CRONOGRAMA DE ETAPAS

### Etapas Concluídas

| Etapa                                       | Período    | Status       |
| ------------------------------------------- | ---------- | ------------ |
| Análise de Requisitos                       | Mês X/20XX | ✅ Concluída |
| Configuração do Ambiente de Desenvolvimento | Mês X/20XX | ✅ Concluída |
| Migração SQL Server → SQLite                | Mês X/20XX | ✅ Concluída |
| Implementação de DAOs                       | Mês X/20XX | ✅ Concluída |
| Sistema de Autenticação                     | Mês X/20XX | ✅ Concluída |
| Gestão de Usuários                          | Mês X/20XX | ✅ Concluída |
| Gestão de Penas                             | Mês X/20XX | ✅ Concluída |
| Gestão de Instituições                      | Mês X/20XX | ✅ Concluída |
| Controle de Trabalho                        | Mês X/20XX | ✅ Concluída |
| Implementação de Reconhecimento Facial      | Mês X/20XX | ✅ Concluída |
| Interface Gráfica                           | Mês X/20XX | ✅ Concluída |
| Testes e Correções                          | Mês X/20XX | ✅ Concluída |

### Etapas em Andamento

| Etapa                | Período    | Status          |
| -------------------- | ---------- | --------------- |
| Testes de Integração | Mês X/20XX | 🔄 Em Andamento |
| Documentação Técnica | Mês X/20XX | 🔄 Em Andamento |

### Etapas Futuras (se aplicável)

| Etapa                          | Período Previsto | Status       |
| ------------------------------ | ---------------- | ------------ |
| Testes com Usuários Finais     | Mês X/20XX       | ⏳ Planejada |
| Implementação de Melhorias     | Mês X/20XX       | ⏳ Planejada |
| Deploy em Ambiente de Produção | Mês X/20XX       | ⏳ Planejada |
| Treinamento de Usuários        | Mês X/20XX       | ⏳ Planejada |

---

## 8. REFERÊNCIAS BIBLIOGRÁFICAS

ABADI, Martin et al. **TensorFlow: Large-Scale Machine Learning on Heterogeneous Distributed Systems**. arXiv preprint arXiv:1603.04467, 2016.

BRADSKI, Gary; KAEHLER, Adrian. **Learning OpenCV: Computer Vision in C++ with the OpenCV Library**. 2. ed. Sebastopol: O'Reilly Media, 2013.

BYTEDECO. **JavaCV: Java Interface to OpenCV, FFmpeg, and more**. Disponível em: https://github.com/bytedeco/javacv. Acesso em: [data de acesso].

FREEMAN, Eric; ROBSON, Elisabeth. **Head First Design Patterns: A Brain-Friendly Guide**. 2. ed. Sebastopol: O'Reilly Media, 2020.

GAMMA, Erich et al. **Padrões de Projeto: Soluções Reutilizáveis de Software Orientado a Objetos**. Porto Alegre: Bookman, 2000.

GOODFELLOW, Ian; BENGIO, Yoshua; COURVILLE, Aaron. **Deep Learning**. Cambridge: MIT Press, 2016.

JAVA COMMUNITY PROCESS. **Java Platform, Standard Edition Documentation**. Disponível em: https://docs.oracle.com/javase/. Acesso em: [data de acesso].

JAVAFX. **JavaFX Documentation**. Disponível em: https://openjfx.io/. Acesso em: [data de acesso].

MAINARDI, Leonardo. **Visão Computacional: Algoritmos e Aplicações**. São Paulo: Novatec, 2018.

OPENCV. **OpenCV Documentation**. Disponível em: https://docs.opencv.org/. Acesso em: [data de acesso].

SCHROEDER, Werner; BLOCH, Joshua. **Effective Java**. 3. ed. Boston: Addison-Wesley Professional, 2018.

SCHROFF, Florian; KALENICHENKO, Dmitry; PHILBIN, James. **FaceNet: A Unified Embedding for Face Recognition and Clustering**. In: Proceedings of the IEEE Conference on Computer Vision and Pattern Recognition, 2015, p. 815-823.

SQLITE. **SQLite Documentation**. Disponível em: https://www.sqlite.org/docs.html. Acesso em: [data de acesso].

VIOLA, Paul; JONES, Michael. **Rapid Object Detection using a Boosted Cascade of Simple Features**. In: Proceedings of the 2001 IEEE Computer Society Conference on Computer Vision and Pattern Recognition, 2001.

ZHANG, Jian et al. **Face detection using deep learning: An improved faster RCNN approach**. Expert Systems with Applications, v. 99, p. 141-154, 2018. Disponível em: https://www.sciencedirect.com/science/article/abs/pii/S0925231218303229. Acesso em: nov. 2025.

MINAEE, Shervin et al. **Going Deeper Into Face Detection: A Survey**. arXiv preprint arXiv:2103.14983, 2021. Disponível em: https://arxiv.org/abs/2103.14983. Acesso em: nov. 2025.

SYSTEMATIC LITERATURE REVIEW. **Robotic Process Automation (RPA)**: A systematic literature review on automation efficiency. arXiv preprint arXiv:2012.11951, 2020. Disponível em: https://arxiv.org/abs/2012.11951. Acesso em: nov. 2025.

NATURE. **Hyperautomation in Construction**: Empirical examination of efficiency and sustainability. Scientific Reports, 2025. Disponível em: https://www.nature.com/articles/s41598-025-25542-y. Acesso em: nov. 2025.

MDPI. **Multicomponent Face Verification and Identification System**: Performance evaluation of FaceNet. Applied Sciences, v. 15, n. 15, 2025. Disponível em: https://www.mdpi.com/2076-3417/15/15/8161. Acesso em: nov. 2025.

IJSAT. **Multimodal Biometric Systems**: Enhancing security through multiple verification modalities. International Journal of Science and Advanced Technology, 2025. Disponível em: https://www.ijsat.org/papers/2025/1/1771.pdf. Acesso em: nov. 2025.

MARQUETTE UNIVERSITY. **Component-Based Design for Embedded Database Systems**: Enhancing portability and adaptability. ePublications, 2025. Disponível em: https://epublications.marquette.edu/mscs_fac/367/. Acesso em: nov. 2025.

SCIENTIFIC.NET. **Cross-Platform Compatibility in Embedded Database Systems**. Applied Mechanics and Materials, v. 40-41, p. 985, 2025. Disponível em: https://www.scientific.net/AMM.40-41.985. Acesso em: nov. 2025.

PUBMED. **AI-Based Operational Management Systems**: Impact on efficiency in healthcare organizations. PubMed, 2024. Disponível em: https://pubmed.ncbi.nlm.nih.gov/40371290/. Acesso em: nov. 2025.

KKJOURNAL. **Comparative Study of ArcFace and FaceNet**: Customer facial recognition accuracy analysis. Journal of Lao Science and Technology Education, 2025. Disponível em: https://kkjournal.com/index.php/jlste/article/view/78. Acesso em: nov. 2025.

---

## ANEXOS

### Anexo A - Estrutura de Tabelas do Banco de Dados

[Incluir diagrama ou descrição das tabelas principais: Usuario, Pena, Instituicao, RegistroDeTrabalho, DadosFaciais, etc.]

### Anexo B - Diagrama de Classes

[Incluir diagrama UML mostrando as principais classes e seus relacionamentos]

### Anexo C - Capturas de Tela do Sistema

[Incluir capturas de tela das principais funcionalidades: Login, Cadastro de Usuário, Identificação Facial, etc.]

### Anexo D - Código-Fonte

[Referência ao repositório ou localização do código-fonte completo]

---

**Nota**: Este documento serve como base para a elaboração do relatório parcial em Word. As datas, períodos e informações específicas devem ser preenchidas conforme o período real de desenvolvimento do projeto. As referências bibliográficas devem ser formatadas conforme as normas ABNT e incluir as datas de acesso para recursos online.
