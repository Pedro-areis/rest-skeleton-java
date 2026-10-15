# rest-skeleton-java

Esqueleto de API REST com CRUD de usuários e autenticação por token (JWT), para reaproveitar no início de novos projetos.

Projeto de estudo: o foco é arquitetura, decisões documentadas e testes, não só o código que funciona.

**Stack:** Java 21, Spring Boot 4, PostgreSQL, Flyway, JUnit e Testcontainers. Arquitetura hexagonal por funcionalidade.

## Pré-requisitos
- Java 21
- Docker Desktop aberto, para os testes (eles sobem um PostgreSQL em container)
- Um PostgreSQL local, só se for rodar a aplicação

## Como rodar a aplicação
1. Crie o banco (por padrão, `rest_skeleton_db`) e ajuste as variáveis abaixo, se o seu ambiente for diferente.
2. Suba a aplicação:
```bash
./mvnw spring-boot:run
```
As migrações do Flyway criam as tabelas na primeira execução. A API responde em `http://localhost:8080`.

### Variáveis de ambiente
| Variável | Padrão | Para que serve |
|---|---|---|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `rest_skeleton_db` | Nome do banco |
| `DB_USER` | `postgres` | Usuário do banco |
| `DB_PASSWORD` | `user` | Senha do banco |
| `JWT_SECRET` | valor de ambiente local | Segredo que assina os tokens (mínimo de 32 bytes). **Defina fora do ambiente local**: o padrão está no repositório e qualquer pessoa poderia forjar tokens (ADR 002) |

O CORS é configurado pela propriedade `api.cors.allowed-origins` (origens separadas por vírgula). Vazia, não libera nenhuma origem (ADR 006).

## Como rodar os testes
```bash
./mvnw test
```
Exige o **Docker aberto**. Os testes de integração usam um PostgreSQL em container (Testcontainers) e não tocam no seu banco local. A primeira execução baixa a imagem e demora mais (ADR 007).

## Rotas
| Método | Rota | Pública | Descrição |
|---|---|---|---|
| POST | `/api/v1/public/users/register` | sim | Cadastra um usuário |
| POST | `/api/v1/public/auth/login` | sim | Login: devolve access token e refresh token |
| POST | `/api/v1/public/auth/refresh` | sim | Troca um refresh token por um novo access token |
| GET | `/api/v1/users/me` | não | Consulta os dados do usuário autenticado |
| PATCH | `/api/v1/users/me` | não | Atualiza os dados do usuário autenticado |
| DELETE | `/api/v1/users/me` | não | Exclui o usuário autenticado |

As rotas não públicas exigem o header `Authorization: Bearer <access token>`.

## Decisões de arquitetura
Cada decisão importante tem um registro com contexto e trade-offs em [`docs/ADRs/`](docs/ADRs/README.md):

- [001](docs/ADRs/001-arquitetura-hexagonal-por-funcionalidade.md): hexagonal organizada por funcionalidade (`user`, `auth`), com o domínio em Java puro, sem Spring.
- [002](docs/ADRs/002-jwt-hs256-access-token-1h.md): JWT HS256, access token de 1 hora e refresh token de 7 dias, sem estado no servidor.
- [003](docs/ADRs/003-hash-de-senha-bcrypt.md): senha com BCrypt, atrás de uma porta, para o domínio não conhecer o algoritmo.
- [004](docs/ADRs/004-transacoes.md): transações adiadas, com a abordagem já definida para quando um caso de uso precisar de várias escritas.
- [005](docs/ADRs/005-tratamento-de-erros-compartilhado.md): um formato de erro para toda a API e um tratador de exceções por funcionalidade.
- [006](docs/ADRs/006-spring-security.md): Spring Security sem estado, rotas públicas por prefixo e 401/403 no mesmo formato de erro.
- [007](docs/ADRs/007-testes-de-integracao.md): testes de integração com PostgreSQL real em container, em vez de banco em memória.

## Documentação
- [`docs/PROJECT.md`](docs/PROJECT.md): objetivo e escopo.
- [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md): requisitos funcionais e não funcionais.
- [`docs/TASKS.md`](docs/TASKS.md): plano de trabalho e andamento.
- [`CLAUDE.md`](CLAUDE.md): contexto e regras para o Claude Code.

`Dockerfile`, `docker-compose.yml` e `.github/workflows/deploy.yml` estão vazios de propósito: containerização e deploy foram adiados (ver `docs/TASKS.md`).
