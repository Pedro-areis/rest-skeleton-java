# 005 — Sem papéis (roles) e sem listagem de usuários

**Data:** 30/09/2026
**Status:** Decidido

## Contexto
Os documentos previam um administrador que consulta e lista usuários (RF03, RF04) e exclui usuários (RF06). O esqueleto será clonado para outros projetos, e cada projeto tem necessidades diferentes de permissão.

## Decisão
Existe apenas o tipo "usuário". Sem coluna de papel, sem administrador, sem endpoint de listagem ou de consulta por e-mail.

## Trade-offs
**Ganhos**
- Esqueleto mais simples.
- Listagem geral removida por privacidade.
- Mais fácil acrescentar papéis depois do que removê-los.

**Custos**
- Quem precisar de administrador terá de implementá-lo.
- RF03, RF04 e parte do RF06 saem dos requisitos.

## Revisar quando
- Um projeto derivado precisar de perfis diferentes.
