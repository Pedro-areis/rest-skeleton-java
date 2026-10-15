# 005 — Tratamento de erros: formato compartilhado e um advice por funcionalidade

**Data:** 12/10/2026
**Status:** Decidido e implementado

## Contexto
O `GlobalExceptionHandler` e o `ErrorResponse` moravam em `user/adapters/inbound`. A funcionalidade `auth` (Task 6) precisa do mesmo formato de erro e de traduzir exceções próprias (credenciais inválidas e token inválido, ambas 401). A dependência entre funcionalidades tem uma só direção, `auth → user` (ADR 001), então o handler de `user` não pode conhecer exceções de `auth`. Também não é possível pôr o status HTTP dentro de `DomainException`, porque o domínio é Java puro.

## Decisão
- `ErrorResponse` vai para `shared/web`, usado por todas as funcionalidades.
- Cada funcionalidade tem o seu `@RestControllerAdvice`, que traduz só as exceções dela (`user` mantém o seu; `auth` ganha o seu).
- Um handler genérico em `shared/web` cuida do que não é de nenhuma funcionalidade: validação (`MethodArgumentNotValidException`), corpo ilegível e o catch-all de `Exception`. Ele tem a menor precedência (`@Order(Ordered.LOWEST_PRECEDENCE)`), e os advices das funcionalidades têm precedência maior, para que o catch-all nunca vença um handler específico.
- `shared/web` não importa nada de `user` nem de `auth`.

## Trade-offs
**Ganhos**
- Um formato de erro só para a API inteira.
- A direção `auth → user` fica intacta, e `DomainException` continua sem dado HTTP.
- Quem remover uma funcionalidade num projeto derivado leva o advice junto, sem tocar nas outras.

**Custos**
- Mais classes do que um handler único.
- A ordem entre os advices precisa ser declarada; sem `@Order`, o catch-all pode capturar exceções que deveriam ter handler específico.
- Na Task 7, `AccessDeniedException` e `AuthenticationException` têm de passar pelo catch-all sem virar 500 (ele fica em `shared/web`, num lugar único).

## Alternativas descartadas
- **Handler único em `shared` conhecendo todas as exceções:** inverte a dependência (`shared → user`, `shared → auth`).
- **Manter tudo em `user` e o `auth` importar de lá:** acopla o formato de erro de toda a API à funcionalidade `user`.
- **Categorias de exceção (`NotFound`, `Conflict`...) em `DomainException`, com um handler por categoria:** exige mexer em todas as exceções atuais e merece ADR próprio, se um dia for necessário.

## Revisar quando
- O número de funcionalidades crescer e repetir handlers parecidos (a opção de categorias de exceção volta à mesa).
