# Desafio Beca Java — API de Gestão Financeira

Projeto desenvolvido para o Desafio Beca Java 2026, utilizando Java, Spring Boot e arquitetura hexagonal.

Este repositório contém o **MS1 — User Service**, responsável pelo gerenciamento e autenticação de usuários. O MS2, voltado às transações financeiras, será desenvolvido em uma etapa separada.

## Tecnologias

- Java 21 e Spring Boot 4.1.2
- Spring Web, Spring Data JPA e Spring Security
- Bean Validation e BCrypt
- PostgreSQL 16 e Flyway
- Maven, JUnit 5, Mockito e JaCoCo
- Swagger / OpenAPI
- Docker e Docker Compose

## Funcionalidades

- Cadastro, listagem, consulta, atualização e exclusão de usuários
- Normalização e unicidade do e-mail, com tratamento de duplicidade
- Senhas protegidas por BCrypt e autenticação HTTP Basic
- Respostas sem exposição da senha ou do hash
- Atualização e exclusão permitidas apenas ao proprietário da conta
- Importação parcial de usuários por CSV, com relatório de erros por linha
- Validação de dados e tratamento centralizado de erros HTTP
- Migrações do banco, documentação Swagger e testes automatizados

## Arquitetura

O MS1 segue uma arquitetura hexagonal simples (**Ports and Adapters**), separando as regras da aplicação das tecnologias externas.

```text
ms1-user/
└── src/main/java/br/com/lucas/ms1_user/
    ├── domain/
    │   ├── model/             # Usuário e linha do CSV
    │   └── exception/         # Exceções de negócio
    ├── application/
    │   ├── dto/               # Resultado da importação
    │   ├── port/in/           # Casos de uso
    │   ├── port/out/          # Interfaces de saída
    │   ├── service/           # Regras de negócio
    │   └── validation/        # Validação dos usuários
    └── adapter/
        ├── controller/        # Endpoints e DTOs HTTP
        ├── csv/               # Leitura do CSV
        ├── exception/         # Tratamento de erros
        ├── persistence/       # JPA e PostgreSQL
        └── security/          # HTTP Basic e BCrypt
```

O fluxo principal é: **requisição HTTP → controller → caso de uso → serviço → porta de saída → adaptador JPA → PostgreSQL**.

As alterações do banco são versionadas pelo Flyway em `ms1-user/src/main/resources/db/migration/` (V1, V2 e V3). O Hibernate utiliza `ddl-auto=validate`.

## Configuração e execução

**Pré-requisitos:** Git e Docker Desktop com Docker Compose. Para executar os testes localmente, utilize Java 21 e o Maven Wrapper incluído no projeto.

Na raiz do repositório, copie o arquivo de exemplo e configure as variáveis do ambiente:

```powershell
Copy-Item .env.example .env
```

O `.env` contém as configurações dos bancos (`USER_DB_*` e `TRANSACTION_DB_*`). **Não envie esse arquivo ao GitHub.**

Para construir e iniciar a API e seu PostgreSQL:

```powershell
docker compose up --build -d ms1-user
```

Para verificar os containers e acompanhar os logs:

```powershell
docker compose ps
docker compose logs -f ms1-user
```

Com a aplicação iniciada:

| Recurso | Endereço |
|---|---|
| API de usuários | http://localhost:8081/users |
| Swagger UI | http://localhost:8081/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8081/v3/api-docs |
| Health check | http://localhost:8081/actuator/health |

O PostgreSQL do MS1 utiliza a porta interna `5432`. No ambiente local, a porta exposta é definida por `USER_DB_PORT` (por exemplo, `5434`). Os dados são persistidos em um volume Docker.

Para parar os serviços sem remover os volumes, utilize `docker compose down`. Evite `docker compose down -v` se quiser preservar os dados cadastrados.

## Endpoints e autenticação

| Método | Endpoint | Descrição | Autenticação |
|---|---|---|---|
| POST | `/users` | Cadastrar usuário | Pública |
| GET | `/users` | Listar usuários | HTTP Basic |
| GET | `/users/{id}` | Consultar por ID | HTTP Basic |
| PUT | `/users/{id}` | Atualizar a própria conta | HTTP Basic + proprietário |
| DELETE | `/users/{id}` | Excluir a própria conta | HTTP Basic + proprietário |
| POST | `/users/import` | Importar usuários por CSV | HTTP Basic |

No HTTP Basic, o **usuário é o e-mail cadastrado** e a **senha é a informada no cadastro**. No Swagger, o botão `Authorize` permite informar essas credenciais.

Exemplo de cadastro (`POST /users`):

```json
{
  "name": "Ana Silva",
  "email": "ana@example.com",
  "password": "SenhaAna123"
}
```

O cadastro retorna **201 Created**, com o header `Location` apontando para `/users/{id}`. A resposta contém ID, nome e e-mail, sem a senha.

## Importação CSV

O endpoint `POST /users/import` recebe um arquivo `multipart/form-data` no campo **`file`**, com o cabeçalho exato `name,email,password`.

```csv
name,email,password
Ana Silva,ana@example.com,SenhaAna123
Pedro Souza,pedro@example.com,123
Maria Oliveira,maria@example.com,SenhaMaria123
```

A importação é **parcial**: linhas válidas são cadastradas, enquanto as inválidas são ignoradas e registradas com número da linha e motivo. Uma falha em uma linha não desfaz os cadastros válidos já realizados.

Exemplo de resposta (`200 OK`):

```json
{
  "totalImported": 2,
  "errors": [
    {
      "line": 3,
      "reason": "A senha deve ter no mínimo 8 caracteres"
    }
  ]
}
```

Regras de importação:

- Máximo de **1 MB por arquivo** e **1.000 linhas de dados não vazias** (sem contar o cabeçalho).
- Linhas com formato incorreto, dados inválidos ou e-mails duplicados são registradas no relatório.
- Cabeçalho inválido e limite de linhas excedido provocam rejeição do arquivo com HTTP `400`.
- Upload acima do limite do Spring pode retornar HTTP `413`.

## Tratamento de erros

A API possui um `GlobalExceptionHandler` para padronizar as respostas.

| Status | Situação |
|---|---|
| `400 Bad Request` | Dados inválidos ou arquivo CSV incorreto |
| `401 Unauthorized` | Autenticação ausente ou inválida |
| `403 Forbidden` | Tentativa de alterar ou excluir conta de outra pessoa |
| `404 Not Found` | Usuário não encontrado |
| `409 Conflict` | E-mail duplicado ou violação de integridade |
| `413 Payload Too Large` | Upload acima do limite permitido |
| `500 Internal Server Error` | Falha inesperada, sem expor detalhes internos |

## Testes e cobertura

Os testes utilizam **JUnit 5** e **Mockito** para verificar cadastro, atualização, validação, leitura e importação parcial do CSV, persistência, controller, autorização e tratamento de erros.

Para executar a suíte completa no Windows, entre na pasta `ms1-user`:

```powershell
cd ms1-user
```

Como o Maven executado no terminal não lê automaticamente o `.env`, carregue as variáveis na sessão do PowerShell:

```powershell
Get-Content ..\.env | ForEach-Object {
    if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2].Trim(), "Process")
    }
}
```

Execute os testes e gere o relatório JaCoCo:

```powershell
.\mvnw.cmd clean verify
Invoke-Item .\target\site\jacoco\index.html
```

## Status do projeto

**MS1 — Gerenciamento de Usuários:** CRUD, segurança, importação parcial de CSV, Flyway, Swagger, Docker e testes implementados.

**MS2 — Transações Financeiras:** próxima etapa do desafio.