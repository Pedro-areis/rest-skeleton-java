# 008 — Refresh token apenas assinado, validade de 7 dias

**Data:** 30/09/2026
**Status:** Decidido

## Contexto
Refresh token permite renovar o access token sem novo login. Guardá-lo no banco permite revogação, mas exige uma tabela nova.

## Decisão
Refresh token JWT apenas assinado, válido por 7 dias, sem persistência.

## Trade-offs
**Ganhos**
- Sem tabela nova e sem consulta ao banco na renovação.
- Menos código.

**Custos**
- Não dá para invalidá-lo antes de expirar: logout não o revoga.
- Um refresh token roubado vale até expirar.

## Revisar quando
- For necessário logout real ou revogação: guardar o refresh token no banco.
