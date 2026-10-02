# Tarefas

Plano de trabalho do esqueleto. As decisões por trás de cada item estão em `docs/adr/`.
Marque `[x]` ao concluir. Uma tarefa por vez, com testes.

**Como retomar uma conversa com a IA:** cole `PROJECT.md`, `REQUIREMENTS.md`, este arquivo e os ADRs relevantes, e diga qual tarefa vai fazer.

## Antes de começar
- [X] Apagar os dados de teste do banco (necessário para a migração `V2` com `NOT NULL`, tarefa 2)

## Fase 1 — Base

- [X] **1. Exceções de domínio e tratador global de erros**
  - Exceções específicas (e-mail já cadastrado, usuário não encontrado etc.)
  - `@ControllerAdvice` mapeando para 400, 404, 409
  - `PostgresUserAdapter` converte `DataIntegrityViolationException` em exceção de domínio
  - Mensagens em português

- [X] **2. Data de nascimento**
  - Migração `V2` (coluna `NOT NULL`), `User`, `UserEntity`, DTOs
  - Regra: não pode ser futura

- [X] **3. Corrigir o `update`**
  - Buscar o usuário por `id` (hoje busca por e-mail)
  - Novo e-mail não pode pertencer a outro usuário
  - Validações no `UpdateUserRequest`
  - Remover a variável não usada em `deleteById`
  - Requisitos: RF05 | Depende de: 1, 2

- [X] **4. Testes unitários do domínio**
  - `User` e `UserService`, com repositório falso (sem Spring e sem banco)
  - Requisitos: RNF03 | Depende de: 3

## Fase 2 — Autenticação

- [ ] **5. `PasswordHasherPort` e adaptador BCrypt**
  - Cadastro e atualização gravam o hash
  - ADR: 009, 010 | Depende de: 4

- [ ] **6. `TokenPort`, adaptador JWT, login e renovação**
  - HS256, access token de 1 hora, refresh token de 7 dias (só assinado)
  - Chave secreta no código por ora (ADR 015)
  - Casos de uso: login e renovação do token
  - ADR: 002 | Requisitos: RF02 | Depende de: 2

- [ ] **7. Spring Security**
  - `SecurityFilterChain`, filtro que lê o token
  - Rotas públicas: cadastro, login, renovação

- [ ] **8. Endpoints `/me`**
  - `GET /me`, `PATCH /me`, `DELETE /me` com `@AuthenticationPrincipal`
  - Remover as rotas com `{userId}`

## Fase 3 — Fechamento

- [ ] **9. Testes de integração dos endpoints**
  - Cadastro, login, renovação, `/me`, erros
  - Requisitos: RNF03 | Depende de: 8

- [ ] **10. Atualizar a documentação**
  - `REQUIREMENTS.md`: remover RF03 e RF04, ajustar RF02 (refresh token) e RF06 (sem administrador), incluir `/me`
  - `PROJECT.md`: remover "apenas ADM"
  - Mover `decisoes/` para `docs/adr/`
  - Depende de: 9

## Adiado (ver ADR 016 e 014)
- Semântica do PATCH com string vazia
- Docker, CI e `deploy.yml`
- Configuração por ambiente e chave do JWT fora do código
- `.gitignore` com marcadores de conflito de merge
- Transações (avisar quando um caso de uso precisar de várias escritas)

## Dúvidas técnicas em aberto
- `@GeneratedValue(UUID)` com `id` já preenchido pelo domínio: conferir o comportamento no `save` (ADR 002)
- Versões do Flyway: conferir com `./mvnw dependency:tree` (ADR 004)
