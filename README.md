# rest-skeleton-java

Esqueleto de API REST com CRUD de usuários e autenticação por token, para reaproveitar no início de novos projetos.

**Stack:** Java 21, Spring Boot 4, PostgreSQL, Flyway, JUnit. Arquitetura hexagonal por funcionalidade.

## Documentação
- [`docs/PROJECT.md`](docs/PROJECT.md): objetivo e escopo.
- [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md): requisitos funcionais e não funcionais.
- [`docs/TASKS.md`](docs/TASKS.md): plano de trabalho e andamento.
- [`docs/ADRs/`](docs/ADRs/README.md): decisões de arquitetura.
- [`CLAUDE.md`](CLAUDE.md): contexto e regras para o Claude Code.

## Como rodar os testes
```bash
./mvnw test
```
O teste `contextLoads` precisa de um PostgreSQL acessível (variáveis `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`). Os demais rodam sem banco.
