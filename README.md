# \# Desafio Beca Java — API de Gestão Financeira

# 

# Projeto desenvolvido como parte do \*\*Desafio Beca Java\*\*, com o objetivo de construir uma API de gestão financeira utilizando Java, Spring Boot, microsserviços e arquitetura hexagonal.

# 

# Este repositório está organizado em microsserviços. A primeira etapa implementada é o \*\*MS1 — Gerenciamento de Usuários\*\*.

# 

# \## MS1 — Gerenciamento de Usuários

# 

# O MS1 é responsável pelo cadastro e gerenciamento de usuários, oferecendo operações CRUD, autenticação HTTP Basic e importação de usuários por arquivo CSV.

# 

# \### Funcionalidades implementadas

# 

# \- Cadastro de usuários.

# \- Listagem de usuários.

# \- Consulta de usuário por ID.

# \- Atualização de usuários.

# \- Exclusão de usuários.

# \- Validação de nome, e-mail e senha.

# \- Normalização e unicidade de e-mail.

# \- Criptografia de senhas com BCrypt.

# \- Autenticação utilizando HTTP Basic.

# \- Respostas da API sem exposição da senha.

# \- Importação de usuários por CSV com transação.

# \- Tratamento de erros HTTP.

# \- Versionamento do banco com Flyway.

# \- Documentação interativa com Swagger/OpenAPI.

# \- Testes automatizados com JUnit e Mockito.

# \- Relatório de cobertura com JaCoCo.

# \- Execução da aplicação com Docker Compose.

# 

# \## Tecnologias utilizadas

# 

# \- Java 21

# \- Spring Boot 4.1.2

# \- Spring Web

# \- Spring Data JPA

# \- Spring Security

# \- Bean Validation

# \- PostgreSQL 16

# \- Flyway

# \- Maven

# \- Docker e Docker Compose

# \- Swagger / OpenAPI

# \- JUnit 5

# \- Mockito

# \- JaCoCo

# 

# \## Arquitetura

# 

# O MS1 utiliza \*\*arquitetura hexagonal (Ports and Adapters)\*\* para separar as regras de negócio da infraestrutura.

# 

# A estrutura principal é:

# 

# ```text

# ms1-user/

# ├── src/

# │   ├── main/

# │   │   ├── java/br/com/lucas/ms1\_user/

# │   │   │   ├── adapter/

# │   │   │   │   ├── controller/

# │   │   │   │   ├── csv/

# │   │   │   │   ├── exception/

# │   │   │   │   ├── persistence/

# │   │   │   │   └── security/

# │   │   │   ├── application/

# │   │   │   │   ├── port/

# │   │   │   │   ├── service/

# │   │   │   │   └── validation/

# │   │   │   └── domain/

# │   │   └── resources/

# │   │       └── db/migration/

# │   └── test/

# ├── Dockerfile

# └── pom.xml

# ```

# 

# \*\*Responsabilidades das camadas:\*\*

# 

# \- \*\*Domain:\*\* modelos e exceções de negócio.

# \- \*\*Application:\*\* casos de uso, serviços, validações e portas.

# \- \*\*Adapters:\*\* comunicação HTTP, persistência, segurança e leitura de CSV.

# \- \*\*Infrastructure:\*\* configuração de banco, Docker e bibliotecas externas.

# 

# \## Pré-requisitos

# 

# Para executar o projeto, é necessário ter:

# 

# \- Git

# \- Docker Desktop com Docker Compose

# \- Java 21 e Maven, caso queira executar a aplicação localmente

# 

# O projeto também possui Maven Wrapper, permitindo executar os comandos Maven sem instalar o Maven separadamente.

# 

# \## Configuração do ambiente

# 

# Na raiz do repositório, existe um arquivo `.env.example` com as variáveis de configuração.

# 

# Crie um arquivo `.env` a partir dele e preencha os valores necessários.

# 

# Exemplo no PowerShell:

# 

# ```powershell

# Copy-Item .env.example .env

# ```

# 

# As variáveis utilizadas pelo ambiente Docker incluem:

# 

# ```text

# USER\_DB\_NAME

# USER\_DB\_USER

# USER\_DB\_PASSWORD

# USER\_DB\_PORT

# 

# TRANSACTION\_DB\_NAME

# TRANSACTION\_DB\_USER

# TRANSACTION\_DB\_PASSWORD

# TRANSACTION\_DB\_PORT

# ```

# 

# \*\*Importante:\*\* o arquivo `.env` contém configurações sensíveis e não deve ser enviado ao GitHub.

# 

# \## Executando com Docker Compose

# 

# Na raiz do repositório, execute:

# 

# ```powershell

# docker compose up --build -d ms1-user

# ```

# 

# Esse comando constrói a imagem da aplicação e inicia o MS1 e o banco PostgreSQL necessário.

# 

# Para verificar os containers:

# 

# ```powershell

# docker compose ps

# ```

# 

# Para consultar os logs:

# 

# ```powershell

# docker compose logs -f ms1-user

# ```

# 

# \*\*Observação:\*\* no ambiente utilizado durante o desenvolvimento, o banco já pertencia ao projeto Compose `desafio\\\_beca-`. Para reutilizar esse ambiente existente, foi utilizado:

# 

# ```powershell

# docker compose -p desafio\_beca- up --build -d ms1-user

# ```

# 

# Não utilize `docker compose down -v` se precisar preservar os dados armazenados nos volumes PostgreSQL.

# 

# \## Acessando a aplicação

# 

# Com o MS1 iniciado, os principais endereços são:

# 

# | Recurso | URL |

# |---|---|

# | API de usuários | http://localhost:8081/users |

# | Swagger UI | http://localhost:8081/swagger-ui/index.html |

# | OpenAPI JSON | http://localhost:8081/v3/api-docs |

# | Health check | http://localhost:8081/actuator/health |

# 

# \## Endpoints do MS1

# 

# | Método | Endpoint | Descrição | Autenticação |

# |---|---|---|---|

# | POST | `/users` | Cadastrar usuário | Pública |

# | GET | `/users` | Listar usuários | HTTP Basic |

# | GET | `/users/{id}` | Buscar usuário por ID | HTTP Basic |

# | PUT | `/users/{id}` | Atualizar usuário | HTTP Basic |

# | DELETE | `/users/{id}` | Excluir usuário | HTTP Basic |

# | POST | `/users/import` | Importar usuários por CSV | HTTP Basic |

# 

# \### Exemplo de cadastro

# 

# \*\*POST `/users`\*\*

# 

# ```json

# {

# &#x20; "name": "Ana Silva",

# &#x20; "email": "ana@exemplo.com",

# &#x20; "password": "Senha123"

# }

# ```

# 

# Em caso de sucesso, a API retorna `201 Created`, incluindo o header `Location`.

# 

# Exemplo de resposta:

# 

# ```json

# {

# &#x20; "id": 1,

# &#x20; "name": "Ana Silva",

# &#x20; "email": "ana@exemplo.com"

# }

# ```

# 

# Por segurança, o campo `password` não é retornado.

# 

# \### Autenticação

# 

# O MS1 utiliza autenticação \*\*HTTP Basic\*\*.

# 

# Após cadastrar um usuário, utilize:

# 

# \- \*\*Username:\*\* e-mail cadastrado.

# \- \*\*Password:\*\* senha informada durante o cadastro.

# 

# No Swagger, clique em \*\*Authorize\*\* para informar as credenciais antes de executar endpoints protegidos.

# 

# As senhas são armazenadas no banco utilizando BCrypt, e não em texto puro.

# 

# \## Importação de usuários por CSV

# 

# O endpoint `POST /users/import` recebe um arquivo enviado como `multipart/form-data`.

# 

# O nome do campo deve ser `file`.

# 

# O arquivo deve conter o cabeçalho:

# 

# ```csv

# name,email,password

# ```

# 

# Exemplo:

# 

# ```csv

# name,email,password

# Ana Silva,ana@exemplo.com,SenhaAna123

# Carlos Souza,carlos@exemplo.com,SenhaCarlos123

# Maria Oliveira,maria@exemplo.com,SenhaMaria123

# ```

# 

# A importação valida os dados antes de cadastrar os usuários.

# 

# A operação é transacional: se ocorrer um erro durante a importação, os cadastros realizados naquela transação são revertidos.

# 

# \## Tratamento de erros

# 

# A aplicação utiliza um tratamento centralizado de exceções.

# 

# | Status | Significado |

# |---|---|

# | `200 OK` | Operação realizada com sucesso |

# | `201 Created` | Usuário criado |

# | `204 No Content` | Usuário excluído |

# | `400 Bad Request` | Dados inválidos ou CSV incorreto |

# | `401 Unauthorized` | Autenticação ausente ou inválida |

# | `404 Not Found` | Usuário não encontrado |

# | `409 Conflict` | E-mail já cadastrado |

# 

# \## Migrações do banco de dados

# 

# O projeto utiliza \*\*Flyway\*\* para versionamento do esquema PostgreSQL.

# 

# As migrações do MS1 ficam em:

# 

# `ms1-user/src/main/resources/db/migration/`

# 

# Migrações implementadas:

# 

# \- `V1\\\_\\\_create\\\_users\\\_table.sql`

# \- `V2\\\_\\\_normalize\\\_and\\\_unique\\\_email.sql`

# \- `V3\\\_\\\_add\\\_password\\\_to\\\_users.sql`

# 

# O Flyway aplica as migrações pendentes durante a inicialização da aplicação.

# 

# \## Testes automatizados

# 

# Os testes do MS1 utilizam \*\*JUnit 5\*\* e \*\*Mockito\*\*.

# 

# Foram implementados testes para as regras de negócio, validação de usuários, leitura de CSV, persistência, controller e tratamento de exceções.

# 

# \### Executar os testes e gerar o relatório

# 

# No Windows, acesse a pasta do MS1:

# 

# ```powershell

# cd ms1-user

# ```

# 

# Para executar o Maven localmente, carregue as variáveis do arquivo `.env` da raiz na sessão atual do PowerShell:

# 

# ```powershell

# Get-Content ..\\.env | ForEach-Object {

# &#x20;   if ($\_ -match '^\\s\*(\[A-Za-z\_]\[A-Za-z0-9\_]\*)=(.\*)$') {

# &#x20;       \[Environment]::SetEnvironmentVariable(

# &#x20;           $matches\[1],

# &#x20;           $matches\[2].Trim(),

# &#x20;           "Process"

# &#x20;       )

# &#x20;   }

# }

# ```

# 

# Em seguida:

# 

# ```powershell

# .\\mvnw.cmd clean verify

# ```

# 

# O Maven executará os testes e o JaCoCo gerará o relatório de cobertura.

# 

# Para abrir o relatório:

# 

# ```powershell

# Invoke-Item .\\target\\site\\jacoco\\index.html

# ```

# 

# \*\*Última cobertura observada durante o desenvolvimento:\*\* aproximadamente \*\*86% das instruções\*\*.

# 

# \## Segurança e boas práticas

# 

# O MS1 utiliza:

# 

# \- Hash de senhas com BCrypt.

# \- HTTP Basic para endpoints protegidos.

# \- DTO de resposta sem o campo de senha.

# \- E-mail normalizado e único.

# \- Bean Validation nas entradas da API.

# \- Variáveis de ambiente para configurações sensíveis.

# \- Migrações versionadas com Flyway.

# \- Transações para importação CSV.

# 

# \## Status do projeto

# 

# \*\*MS1 — Gerenciamento de Usuários:\*\* implementação funcional concluída, incluindo CRUD, segurança, importação CSV, documentação Swagger, testes e execução com Docker.

# 

# \*\*MS2 — Transações:\*\* etapa separada do desafio, a ser desenvolvida posteriormente.


