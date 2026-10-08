# Tarefas

Plano de trabalho do esqueleto. As decisões por trás de cada item estão em `docs/ADRs/`.
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

- [x] **2. Data de nascimento**
  - Migração `V2` (coluna `NOT NULL`), `User`, `UserEntity`, DTOs
  - Regra: não pode ser futura

- [x] **3. Corrigir o `update`**
  - Buscar o usuário por `id` (hoje busca por e-mail)
  - Novo e-mail não pode pertencer a outro usuário
  - Validações no `UpdateUserRequest`
  - Remover a variável não usada em `deleteById`
  - Requisitos: RF05 | Depende de: 1, 2

- [x] **4. Testes unitários do domínio**
  - `User` e `UserService`, com repositório falso (sem Spring e sem banco)
  - Requisitos: RNF03 | Depende de: 3

## Fase 2 — Autenticação

- [x] **5. `PasswordHasherPort` e adaptador BCrypt**
  - Cadastro e atualização gravam o hash
  - ADR: 003 | Depende de: 4

- [ ] **6. `TokenPort`, adaptador JWT, login e renovação**
  - HS256, access token de 1 hora, refresh token de 7 dias (só assinado)
  - Casos de uso: login e renovação do token
  - Plano ainda **aguardando aprovação** (ao aprovar, registrar no ADR 002):
    - Biblioteca Nimbus em vez de JJWT (o JJWT usa Jackson 2; o Spring Boot 4 usa Jackson 3)
    - `sub` do token = id do usuário (o e-mail pode mudar)
    - Claim `type` (`access` ou `refresh`), para o refresh token não servir como access token
    - A renovação devolve só um novo access token
    - Mesmo erro e mesma mensagem para e-mail inexistente e senha errada
    - Chave secreta vinda de propriedade/variável de ambiente, não de constante no código (decidir o nome: já existe `api.security.token.secret` no `application.properties`)
    - `Clock` injetado no adaptador JWT, para os testes controlarem o tempo
    - Antes de começar: mover `GlobalExceptionHandler` e `ErrorResponse` para um pacote compartilhado (`shared/web`)
  - ADR: 002 | Requisitos: RF02 | Depende de: 5

- [ ] **7. Spring Security**
  - `SecurityFilterChain`, filtro que lê o token
  - Rotas públicas: cadastro, login, renovação
  - O `catch-all` do `GlobalExceptionHandler` não pode engolir `AccessDeniedException` e `AuthenticationException`, senão 401/403 viram 500
  - Depende de: 6

- [ ] **8. Endpoints `/me`**
  - `GET /me`, `PATCH /me`, `DELETE /me` com `@AuthenticationPrincipal`
  - Remover as rotas com `{userId}`: com a segurança ligada, qualquer usuário autenticado editaria ou excluiria qualquer id
  - Depende de: 7

## Fase 3 — Fechamento

- [ ] **9. Testes de integração dos endpoints**
  - Cadastro, login, renovação, `/me`, erros, com PostgreSQL real
  - Conferir que o `save` mantém o id gerado pelo domínio (ver "Dúvidas técnicas em aberto")
  - Requisitos: RNF03 | Depende de: 8

- [ ] **10. Revisão final da documentação**
  - Conferir `REQUIREMENTS.md`, `PROJECT.md`, `docs/ADRs/` e `README.md` contra o que foi de fato construído
  - Depende de: 9

## Adiado (ver ADR 004 para transações)
- Semântica do PATCH com string vazia
- `Dockerfile`, `docker-compose.yml` e `deploy.yml` (hoje vazios)
- Configuração por ambiente completa
- Transações (avisar quando um caso de uso precisar de várias escritas)
- Diferença de tempo no login entre e-mail existente e inexistente
- Revogação de refresh token e logout
- `Persistable` na entidade, para evitar o `SELECT` antes do `INSERT`
- Objeto de comando (`CreateUserCommand`) quando os parâmetros dos casos de uso crescerem
- Hasher "delegante", se um dia migrar de BCrypt para Argon2 mantendo usuários antigos

## Dúvidas técnicas em aberto
- Com o `id` gerado pelo domínio, o `save` faz `SELECT` e depois `INSERT` (não só o `INSERT`). Aceito por ora; conferir na Task 9 que o id é mantido.
