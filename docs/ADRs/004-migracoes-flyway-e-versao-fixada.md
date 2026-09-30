# 004 — Migrações com Flyway e versão do módulo PostgreSQL fixada

**Data:** 30/09/2026
**Status:** Existente no código; pin mantido por decisão

## Contexto
O esquema é criado por migrações (`V1__creates_table_users.sql`), e não por `ddl-auto`. O `flyway-database-postgresql` tem versão fixada (11.3.2), enquanto o `flyway-core` vem pela versão gerenciada pelo Spring Boot.

## Decisão
Manter as migrações Flyway e o pin da versão.

## Trade-offs
**Ganhos**
- Esquema versionado e reproduzível.
- Pin evita mudanças inesperadas de versão.

**Custos**
- Versões diferentes entre `flyway-core` e o módulo do Postgres podem causar erro na inicialização.
- Conferir com `./mvnw dependency:tree` (linhas do `flyway`). Se houver erro no Flyway, começar por aí.

## Revisar quando
- Ocorrer erro de Flyway na inicialização ou ao atualizar o Spring Boot.
