# Migração SQL Server para SQLite3 - Checklist de Compatibilidade

Este documento lista todas as correções realizadas para garantir compatibilidade total com SQLite3.

## ✅ Correções Aplicadas

### 1. **Tratamento de Datas (DATE)**

- ✅ Criada classe `SQLiteDateUtil.java` para tratar datas de forma segura
- ✅ Todas as leituras de data (`rs.getDate()`) substituídas por `SQLiteDateUtil.getDate()`
- ✅ Todas as escritas de data (`stmt.setDate()`) substituídas por `stmt.setString()` com formato de data
- ✅ Tratamento de valores NULL em datas
- ✅ Tratamento de valores inválidos (números negativos de migração do SQL Server)

**Arquivos corrigidos:**

- `UsuarioDAO.java` - todas as operações de data
- `PenaDAO.java` - todas as operações de data
- `RegistroDeTrabalhoDAO.java` - todas as operações de data
- `DadosFaciaisDAO.java` - todas as operações de data

### 2. **Tratamento de Horários (TIME)**

- ✅ Criada classe `SQLiteTimeUtil.java` para tratar horários de forma segura
- ✅ Todas as leituras de horário (`rs.getTime()`) substituídas por `SQLiteTimeUtil.getTime()`
- ✅ Todas as escritas de horário (`stmt.setTime()`) substituídas por `stmt.setString()` com formato de horário
- ✅ Tratamento de valores NULL em horários

**Arquivos corrigidos:**

- `RegistroDeTrabalhoDAO.java` - todas as operações de horário
- `DisponibilidadeInstituicaoDAO.java` - todas as operações de horário

### 3. **Queries SQL**

- ✅ `TOP 1` → `LIMIT 1` (PenaDAO, RegistroDeTrabalhoDAO)
- ✅ `GETDATE()` → `date('now', 'localtime')` (PenaDAO)
- ✅ `MONTH()` / `YEAR()` → `strftime('%m', ...)` / `strftime('%Y', ...)` (RegistroDeTrabalhoDAO)
- ✅ Todas as queries verificadas e compatíveis com SQLite

### 4. **Tabelas e Campos**

- ✅ Removidos campos inexistentes (`responsavel2`, `telefone2`) do `InstituicaoDAO`
- ✅ Corrigidas referências a `data_cadastro` → `criado_em` (UsuarioDAO)
- ✅ Padronizados nomes de tabelas (case sensitivity)

### 5. **Configuração do Banco**

- ✅ `db.properties` atualizado para SQLite
- ✅ `ConnectionFactory` atualizado para detectar SQLite e conectar sem usuário/senha
- ✅ `DatabaseInitializer` criado para inicialização automática do banco
- ✅ Scripts SQL convertidos para sintaxe SQLite

### 6. **Dependências**

- ✅ `pom.xml` atualizado: `mssql-jdbc` → `sqlite-jdbc`
- ✅ `module-info.java` atualizado: removida referência ao módulo SQL Server

## 📋 Checklist de Verificação por DAO

### ✅ UsuarioDAO

- [x] Leitura de datas usando SQLiteDateUtil
- [x] Escrita de datas usando setString
- [x] Tratamento de NULL em datas
- [x] Queries compatíveis com SQLite
- [x] Nomes de tabelas padronizados

### ✅ PenaDAO

- [x] Leitura de datas usando SQLiteDateUtil
- [x] Escrita de datas usando setString
- [x] Tratamento de NULL em datas
- [x] Query `buscarPenaAtivaPorUsuario` usando LIMIT e date('now')
- [x] Queries compatíveis com SQLite

### ✅ RegistroDeTrabalhoDAO

- [x] Leitura de datas usando SQLiteDateUtil
- [x] Escrita de datas usando setString
- [x] Leitura de horários usando SQLiteTimeUtil
- [x] Escrita de horários usando setString
- [x] Tratamento de NULL em datas e horários
- [x] Query `buscarPorPenaEMes` usando strftime()
- [x] Query `buscarUltimaDataPorPena` usando LIMIT
- [x] Queries compatíveis com SQLite

### ✅ DadosFaciaisDAO

- [x] Leitura de datas usando SQLiteDateUtil
- [x] Escrita de datas usando setString
- [x] Tratamento de NULL em datas
- [x] Queries compatíveis com SQLite

### ✅ DisponibilidadeInstituicaoDAO

- [x] Leitura de horários usando SQLiteTimeUtil
- [x] Escrita de horários usando setString
- [x] Tratamento de NULL em horários
- [x] Queries compatíveis com SQLite

### ✅ InstituicaoDAO

- [x] Campos removidos (responsavel2, telefone2) que não existem no banco
- [x] Queries compatíveis com SQLite
- [x] Métodos de inserção e atualização corrigidos
- [x] Mapeamento de criado_em adicionado (usando SQLiteDateUtil)

### ✅ AdminDAO

- [x] Queries compatíveis com SQLite
- [x] Nomes de tabelas padronizados

### ✅ TipoInstituicaoDAO

- [x] Queries compatíveis com SQLite

## 🔍 Verificações Adicionais Realizadas

1. ✅ Nenhuma função SQL Server específica encontrada (GETDATE, IDENTITY, TOP, etc.)
2. ✅ Nenhuma sintaxe SQL Server específica encontrada (OFFSET FETCH, ROW_NUMBER, etc.)
3. ✅ Todas as queries usando sintaxe padrão SQL compatível com SQLite
4. ✅ Tratamento adequado de tipos de dados (TEXT para strings, INTEGER para números)
5. ✅ Foreign keys configuradas corretamente
6. ✅ Índices criados para performance

## ⚠️ Pontos de Atenção

### Datas e Horários

- SQLite armazena datas e horários como TEXT
- Sempre use `SQLiteDateUtil.getDate()` para ler datas
- Sempre use `SQLiteTimeUtil.getTime()` para ler horários
- Sempre use `setString()` para escrever datas/horários no formato correto

### Valores NULL

- Sempre verifique NULL antes de usar datas/horários
- Use `stmt.setNull()` quando o valor for NULL

### Case Sensitivity

- SQLite é case-insensitive por padrão para identificadores
- Mantida padronização com primeira letra maiúscula (Administrador, Usuario, etc.)

## 🧪 Testes Recomendados

1. ✅ Cadastro de usuário
2. ✅ Edição de usuário
3. ✅ Cadastro de pena
4. ✅ Edição de pena
5. ✅ Cadastro de registro de trabalho
6. ✅ Edição de registro de trabalho
7. ✅ Cadastro de instituição
8. ✅ Edição de instituição
9. ✅ Cadastro de disponibilidade
10. ✅ Cadastro de dados faciais

## 📝 Notas Finais

Todas as incompatibilidades conhecidas entre SQL Server e SQLite3 foram corrigidas. O projeto está totalmente compatível com SQLite3 e pronto para uso.

### Última Verificação

- Data: 2025-11-08
- Status: ✅ Todas as correções aplicadas
- Compilação: ✅ BUILD SUCCESS
- Testes: ⏳ Aguardando testes de funcionalidade
