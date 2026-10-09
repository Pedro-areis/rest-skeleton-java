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

- [x] **5.1 Regra de senha mínima**
  - Regra (RF01): mínimo de 8 caracteres, sem exigência de maiúscula, número ou símbolo
  - Vale no cadastro e na troca de senha (`PATCH`)
  - O máximo de 72 bytes já existe, no adaptador BCrypt (ADR 003)
  - **Decisões tomadas:**
    - A regra mora num método privado do `UserService` (`validatePassword`)
    - Reusa `InvalidPasswordException`, com a mensagem cobrindo mínimo e máximo
    - Conta-se com `String.length()` e a senha não é alterada (sem `trim`); espaços contam como caracteres
    - Senha só de espaços não é aceita: no cadastro o `@NotBlank` rejeita e no `PATCH` continua significando "não alterar"
    - Validação só no domínio, sem `@Size` no DTO (um `@Size(min = 8)` rejeitaria `""` e quebraria o "vazio não altera" do `PATCH`)
  - Requisitos: RF01, RF04, RNF03 | Depende de: 5

- [ ] **6. `TokenPort`, adaptador JWT, login e renovação**
  - **Decidido** (ADR 002): HS256, access token de 1 hora, refresh token de 7 dias (só assinado)
  - Casos de uso: login (e-mail + senha → access e refresh token) e renovação (refresh token → novo access token)
  - **Decisões em aberto** (do dono do projeto, uma por vez; as aprovadas vão para o ADR 002 ou 003):
    - **Biblioteca JWT** (fica dentro do adaptador, atrás do `TokenPort`):
      - JJWT (terceiro)
      - Nimbus JOSE+JWT direto
      - `NimbusJwtEncoder`/`NimbusJwtDecoder` do Spring Security (embrulham o Nimbus)
      - Nota: o resumo dizia que o JJWT usa Jackson 2 e o Boot 4 usa Jackson 3. **Não foi verificado**; conferir nas dependências antes de decidir
    - **O que o `sub` do token guarda:** id do usuário ou e-mail (o e-mail pode mudar)
    - **Como diferenciar access de refresh token:** claim `type`, ou outro mecanismo (por exemplo, chaves ou durações distintas)
    - **O que a renovação devolve:** só um novo access token, ou um par novo (access e refresh)
    - **Mensagem de erro no login:** a mesma para e-mail inexistente e senha errada, ou mensagens distintas
    - **Onde fica a chave secreta:** propriedade lida de variável de ambiente (já existe `api.security.token.secret` no `application.properties`; decidir se esse nome fica) ou outra forma
    - **Controle do tempo nos testes:** `Clock` injetado no adaptador, ou outra técnica
    - **Pré-requisito estrutural:** `GlobalExceptionHandler` e `ErrorResponse` hoje estão em `user`. O `auth` vai precisar deles e a direção de dependência é `auth → user`, então decidir para onde vão (por exemplo, um pacote compartilhado `shared/web`)
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
