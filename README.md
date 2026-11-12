# Desafio Beca Java — API de Gestão Financeira

Projeto do Desafio Beca Java 2026 composto por dois microsserviços Spring Boot, bancos PostgreSQL independentes e arquitetura hexagonal.

- **MS1 — User Service:** cadastro, autenticação e importação de usuários.
- **MS2 — Transaction Service:** transações, saldo, consultas, análise de despesas, conversão de moedas e relatório CSV.

## Tecnologias

- Java 21 e Spring Boot 4.1.2
- Spring Web MVC, Spring Data JPA, Spring Security e Bean Validation
- PostgreSQL 16 e Flyway
- Maven, JUnit 5, Mockito e JaCoCo
- Swagger / OpenAPI
- Docker e Docker Compose
- MockAPI para saldos e limites
- BrasilAPI para cotações de USD e EUR

## Funcionalidades

### MS1 — Usuários

- CRUD de usuários com proteção da senha por BCrypt
- Autenticação HTTP Basic
- Acesso restrito aos dados do próprio usuário
- Normalização e unicidade do e-mail
- Importação parcial por CSV com erros por linha e campo
- Respostas HTTP padronizadas

### MS2 — Transações

- Criação de depósitos, saques, transferências e compras
- Aprovação ou rejeição conforme saldo, limite por operação e limite diário
- Atualização do saldo na MockAPI
- Autenticação das credenciais pelo MS1
- Consulta por ID e listagem com filtros, sempre limitadas ao usuário autenticado
- Consulta de saldo e limites
- Conversão de valores para USD e EUR pela BrasilAPI
- Análise de despesas por categoria, dia e mês
- Exportação de transações em CSV
- Respostas padronizadas para validação e erros HTTP

## Arquitetura

Os dois microsserviços seguem uma arquitetura hexagonal simples:

```text
domain/                  regras e modelos de domínio
application/
├── port/in/             casos de uso oferecidos pela aplicação
├── port/out/            contratos para serviços externos e persistência
├── service/             regras de negócio
└── dto/                 dados usados entre as camadas
adapter/
├── controller/          endpoints HTTP
├── persistence/         JPA e PostgreSQL
├── client/              integrações externas
├── exception/           tratamento de erros
└── security/            autenticação e autorização
```

O MS1 e o MS2 possuem bancos e migrações Flyway próprios. O Hibernate utiliza `ddl-auto=validate`.

## Configuração

Pré-requisitos: Git, Docker Desktop com Docker Compose e Java 21 para executar testes localmente.

Na raiz do repositório, crie o arquivo local de variáveis:

```powershell
Copy-Item .env.example .env
```

Preencha no `.env` as variáveis `USER_DB_*` e `TRANSACTION_DB_*`. Esse arquivo não deve ser enviado ao Git.

Configurações opcionais do MS2:

| Variável | Finalidade | Valor padrão |
|---|---|---|
| `MS1_BASE_URL` | Endereço do MS1 | `http://localhost:8081` |
| `MOCK_API_ACCOUNT_URL` | Recurso de contas da MockAPI | URL configurada no projeto |
| `BRASIL_API_BASE_URL` | Endereço da BrasilAPI | `https://brasilapi.com.br` |
| `HTTP_CLIENT_CONNECT_TIMEOUT` | Timeout de conexão | `3s` |
| `HTTP_CLIENT_READ_TIMEOUT` | Timeout de leitura | `5s` |

## Execução com Docker

Na raiz do repositório:

```powershell
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

| Recurso | Endereço |
|---|---|
| MS1 | http://localhost:8081 |
| Swagger do MS1 | http://localhost:8081/swagger-ui/index.html |
| Health do MS1 | http://localhost:8081/actuator/health |
| MS2 | http://localhost:8082 |
| Swagger do MS2 | http://localhost:8082/swagger-ui/index.html |
| Health do MS2 | http://localhost:8082/actuator/health |

Para parar os serviços sem apagar os volumes:

```powershell
docker compose down
```

Não use `docker compose down -v` se quiser preservar os dados dos bancos.

## Endpoints do MS1

| Método | Endpoint | Descrição | Autenticação |
|---|---|---|---|
| POST | `/users` | Cadastrar usuário | Pública |
| GET | `/users` | Consultar o próprio usuário | HTTP Basic |
| GET | `/users/{id}` | Consultar por ID | HTTP Basic + proprietário |
| PUT | `/users/{id}` | Atualizar a própria conta | HTTP Basic + proprietário |
| DELETE | `/users/{id}` | Excluir a própria conta | HTTP Basic + proprietário |
| POST | `/users/import` | Importar usuários por CSV | HTTP Basic |

No HTTP Basic do MS1, o usuário é o e-mail cadastrado e a senha é a informada no cadastro.

Exemplo de cadastro:

```json
{
  "name": "Ana Silva",
  "email": "ana@example.com",
  "password": "SenhaAna123"
}
```

## Importação de usuários por CSV

O endpoint `POST /users/import` recebe `multipart/form-data` no campo `file`.

O cabeçalho aceita letras maiúsculas ou minúsculas, espaços ao redor dos nomes e os separadores vírgula ou ponto e vírgula. As colunas são:

```csv
name;email;password
Ana Silva;ana.silva@example.com;SenhaAna123
```

O arquivo exigido como exemplo está em [`examples/users-import-example.csv`](examples/users-import-example.csv).

Regras principais:

- arquivo de até 1 MB;
- no máximo 1.000 linhas de dados não vazias;
- importação parcial, preservando as linhas válidas;
- erros com linha, campo e motivo;
- senha com no mínimo oito caracteres.

## Endpoints do MS2

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/transactions` | Criar uma transação |
| GET | `/transactions` | Listar transações com filtros opcionais |
| GET | `/transactions/{id}` | Consultar uma transação por ID |
| GET | `/balance` | Consultar saldo e limites |
| GET | `/expenses/analysis` | Analisar despesas por período |
| GET | `/transactions/report` | Baixar relatório CSV |

Todos os endpoints do MS2 exigem HTTP Basic. O MS2 envia as credenciais ao MS1 e usa o ID retornado como proprietário das operações.

Filtros disponíveis em `GET /transactions`:

- `type`: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER` ou `PURCHASE`;
- `startDate`: data inicial em `yyyy-MM-dd`;
- `endDate`: data final em `yyyy-MM-dd`.

Exemplo de criação:

```json
{
  "type": "PURCHASE",
  "amount": 100.00,
  "description": "Compra de mercado",
  "category": "ALIMENTACAO"
}
```

O horário da transação é definido pelo servidor no fuso `America/Sao_Paulo`.

## Conversão de moedas

As consultas de transações retornam conversões para USD e EUR. A aplicação consulta a BrasilAPI usando a data da transação.

Quando não há cotação nessa data, como em fins de semana, feriados ou no dia atual, o MS2 procura a última cotação disponível nos sete dias anteriores. O campo `rateDate` sempre informa a data real da cotação utilizada e pode ser anterior à data da transação.

Se nenhuma cotação for encontrada, a consulta continua funcionando com `available: false` e os valores da conversão nulos.

## Tratamento de erros

As APIs utilizam respostas padronizadas. Erros de Bean Validation no MS2 incluem campo e mensagem:

```json
{
  "status": 400,
  "message": "Dados inválidos",
  "errors": [
    {
      "field": "amount",
      "message": "O valor da transação deve ser maior que zero"
    }
  ]
}
```

| Status | Situação |
|---|---|
| `400 Bad Request` | Dados ou período inválidos |
| `401 Unauthorized` | Credenciais inválidas ou ausentes |
| `403 Forbidden` | Operação não autorizada |
| `404 Not Found` | Recurso não encontrado |
| `409 Conflict` | Duplicidade ou conflito de integridade |
| `413 Payload Too Large` | CSV acima do limite |
| `503 Service Unavailable` | Dependência externa indisponível |

## Testes e cobertura

Para testar cada microsserviço:

```powershell
cd ms1-user
.\mvnw.cmd clean verify

cd ..\ms2-transaction
.\mvnw.cmd clean verify
```

Os relatórios JaCoCo são gerados em `target/site/jacoco/index.html` dentro de cada microsserviço.

## Status

- **MS1:** CRUD, segurança, importação CSV, Flyway, Swagger, Docker e testes implementados.
- **MS2:** transações, segurança integrada ao MS1, saldos, limites, BrasilAPI, análises, relatório CSV, Flyway, Swagger, Docker e testes implementados.
