# 002 — Modelo de domínio separado do modelo JPA

**Data:** 30/09/2026
**Status:** Existente no código (não documentada antes) e mantida

## Contexto
`User` (domínio) e `UserEntity` (JPA) são classes diferentes, com conversão explícita (`toDomain`, `fromDomain`). O ID (UUID) é gerado no domínio.

## Decisão
Manter a separação e o UUID gerado no domínio.

## Trade-offs
**Ganhos**
- O domínio não conhece JPA nem anotações de persistência.
- O modelo do banco pode mudar sem afetar as regras.

**Custos**
- Código de mapeamento a manter.
- Dúvida técnica em aberto: com `@GeneratedValue(UUID)` na entidade e `id` já preenchido, o Spring Data trata a entidade como existente (`merge`). Verificar na prática.

## Revisar quando
- O mapeamento virar fonte frequente de erros.
