# 006 — Endpoints em `/me` com `@AuthenticationPrincipal`

**Data:** 30/09/2026
**Status:** Decidido

## Contexto
O usuário só pode ver, alterar e excluir a si mesmo. Rotas com `{userId}` na URL exigiriam comparar o ID da URL com o do token.

## Decisão
`GET /me`, `PATCH /me` e `DELETE /me`. O ID do usuário vem do token, injetado no controller por `@AuthenticationPrincipal`. Não há `{userId}` na URL.

## Trade-offs
**Ganhos**
- Impossível alterar ou excluir outro usuário passando um ID diferente.
- Rotas mais simples.

**Custos**
- Sem consulta a um usuário específico por ID (coerente com a decisão 005).
- O controller depende do Spring Security (aceitável: é adaptador de entrada).

## Revisar quando
- Houver necessidade de operar sobre outros usuários (por exemplo, um administrador).
