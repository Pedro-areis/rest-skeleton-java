# 010 — Hash e token atrás de portas de saída

**Data:** 30/09/2026
**Status:** Decidido

## Contexto
BCrypt e JWT são detalhes de tecnologia. O domínio deve continuar sem depender deles nem do Spring.

## Decisão
- `PasswordHasherPort` com adaptador BCrypt.
- `TokenPort` com adaptador JWT.
- Spring Security (filtro, `SecurityFilterChain`, `@AuthenticationPrincipal`) fica no adaptador de entrada.
- O login é regra de negócio e fica no domínio, usando as duas portas.

## Trade-offs
**Ganhos**
- Trocar tecnologia exige só um adaptador novo.
- Testes do `UserService` com hasher e token falsos: sem Spring, sem banco e sem custo do BCrypt.
- Exemplo clássico de "detalhe técnico atrás de porta", bom para aprendizado.

**Custos**
- Duas interfaces e dois adaptadores a mais.
- Há quem considere exagero, já que essas tecnologias raramente são trocadas.

## Revisar quando
- O custo extra deixar de compensar em um projeto derivado.
