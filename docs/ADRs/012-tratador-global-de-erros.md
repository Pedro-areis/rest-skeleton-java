# 012 — Tratador global de erros e exceções de domínio

**Data:** 30/09/2026
**Status:** Decidido

## Contexto
Hoje o domínio lança `IllegalArgumentException` para tudo, sem tratador global, e os erros de negócio tendem a virar HTTP 500. Além disso, o e-mail duplicado em cadastros simultâneos só é barrado pela constraint `UNIQUE` do banco.

## Decisão
- Exceções de domínio específicas (por exemplo, e-mail já cadastrado, usuário não encontrado).
- Tratador global (`@ControllerAdvice`) no adaptador de entrada, mapeando para 400, 404, 409 etc.
- O `PostgresUserAdapter` converte a exceção do banco (`DataIntegrityViolationException`) em exceção de domínio.
- Mensagens de erro em português.

## Trade-offs
**Ganhos**
- Status HTTP corretos e mensagens consistentes.
- O adaptador traduz o erro da tecnologia para a linguagem do domínio (hexagonal).

**Custos**
- Mais classes de exceção e um componente novo.
- Formato da resposta de erro ainda precisa ser definido.

## Revisar quando
- Surgirem erros que o mapeamento atual não cubra.
