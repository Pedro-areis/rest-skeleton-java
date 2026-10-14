# rest-skeleton-java

Esqueleto de API REST com CRUD de usuários e autenticação por token, para reaproveitar no início de novos projetos.
Java 21, Spring Boot 4.0.x, PostgreSQL, Flyway, JUnit. Projeto de **estudo**: o dono quer aprender arquitetura e a trabalhar com IA.

## Leia antes de qualquer tarefa
- `docs/PROJECT.md`: o que é o projeto, o escopo e o que está fora dele.
- `docs/REQUIREMENTS.md`: o que o sistema deve fazer (RF/RNF).
- `docs/TASKS.md`: plano de trabalho. É a fonte de verdade do que foi feito e do que falta.
- `docs/ADRs/`: decisões de arquitetura, com contexto e trade-offs. Se for contrariar um ADR, avise antes.

## Comandos
- Testes: `./mvnw test` (precisa do **Docker Desktop aberto**: os testes de integração sobem um PostgreSQL em container via Testcontainers, ver ADR 007). Não usa o seu PostgreSQL local.
- Testes de integração (`*IntegrationTest`, base em `support/AbstractIntegrationTest`) rodam no mesmo comando. A primeira execução baixa a imagem do PostgreSQL e demora mais.

## Arquitetura (hexagonal por funcionalidade, ver ADR 001)
Pacote base: `dev.pedroreis.rest_skeleton_java`. Cada funcionalidade (hoje `user` e `auth`) tem `domain`, `ports` (`inbound`/`outbound`) e `adapters` (`inbound`/`outbound`).

**Regra de dependência (a mais importante):**
- `domain` não importa Spring, JPA, Jackson nem nada de `adapters`. Só Java puro.
- `adapters` dependem de `ports` e `domain`, nunca o contrário.
- Entre funcionalidades a direção é uma só: `auth → user`.
- `shared/web` (`ErrorResponse`, `GlobalExceptionHandler` genérico) não importa nada de `user` nem de `auth`. Cada funcionalidade tem o seu advice de exceções (ADR 005).
- O `UserService` é registrado como bean à mão em `config/UserBeanConfig`, sem `@Service`.

**Convenções já decididas:**
- DTO valida presença e formato (Bean Validation). O domínio é dono das regras de negócio.
- Erros de negócio são `DomainException` e filhas, traduzidas em `GlobalExceptionHandler`. Mensagens em português.
- Hash de senha e geração de token ficam atrás de portas (`PasswordHasherPort` e `TokenPort`).
- O id do usuário é gerado pelo domínio, não pelo banco.
- Testes de domínio usam fakes (`FakeUserRepository`, `FakePasswordHasher`), sem Spring e sem banco.
- Schema só muda por migração Flyway (`src/main/resources/db/migration`). `ddl-auto=validate`.

## Como trabalhar neste repositório
- **Uma tarefa do `TASKS.md` por vez**, com testes junto. Marque `[x]` só quando estiver realmente pronta.
- Tarefa não trivial: proponha um plano e espere aprovação antes de codar.
- Uma branch por tarefa (`feature/...`, `docs/...`). Nunca commitar direto na `main`.
- Commits pequenos, em português, com prefixo (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`).
- O dono revisa todos os PRs. Abra o PR, mas não faça merge.
- Mudou uma decisão? Atualize o ADR **antes** e deixe o `TASKS.md` só referenciá-lo.
- Se encontrar divergência entre documentação e código, avise em vez de escolher um lado em silêncio.

## Cuidados
- `application.properties` está em UTF-8 (convertido na Task 9; antes era ISO-8859-1, o que quebrava a cópia de recursos do Maven). Mantenha UTF-8 ao editar.
- O projeto está dentro do OneDrive. Se o Git ou o Maven reclamarem de arquivo travado, suspeite da sincronização.
- `Dockerfile`, `docker-compose.yml` e `.github/workflows/deploy.yml` estão vazios de propósito (adiados).
