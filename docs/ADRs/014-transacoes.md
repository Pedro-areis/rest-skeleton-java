# 014 — Transações

**Data:** 30/09/2026
**Status:** Adiado

## Contexto
Cada caso de uso faz no máximo uma escrita no banco, e uma escrita isolada já é atômica. Não há `@Transactional` no código. O domínio não deve importar anotações do Spring.

## Decisão
Não implementar nada agora. Quando um caso de uso precisar de várias escritas do tipo "tudo ou nada" (por exemplo, transferência na web wallet), usar a Opção A: um decorator na borda, com `@Transactional`, que implementa as portas de entrada e delega ao `UserService`.

## Trade-offs
**Ganhos (Opção A)**
- Domínio continua sem Spring.
- A transação envolve o caso de uso inteiro.

**Custos (Opção A)**
- Uma classe a mais, com métodos repetitivos.
- Cuidado com a injeção: `UserService` e o decorator implementam as mesmas interfaces (usar `@Primary` ou registrar só um).

## Alternativas
- Porta de saída de transação (`TransactionPort`): domínio sem Spring, mais trabalhoso.
- `@Transactional` direto no `UserService`: mais simples, mas o módulo de domínio passa a depender de `spring-tx`.

## Revisar quando
- Surgir um caso de uso com mais de uma escrita.
