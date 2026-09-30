# 003 — A camada web conhece a classe de domínio `User`

**Data:** 30/09/2026
**Status:** Decidido (manter como está)

## Contexto
`UserController` e `UserResponse` importam `domain.User`, enquanto o banco não conhece `User` (usa `UserEntity`). Há uma assimetria entre os dois lados.

## Decisão
Manter a web usando `User` diretamente. O `UserResponse` continua escolhendo os campos expostos (a senha nunca sai).

## Trade-offs
**Ganhos**
- Menos código e menos mapeamento.
- Coerente com um esqueleto simples.

**Custos**
- Uma mudança em `User` pode afetar a API sem que se perceba.
- O lado web fica menos isolado que o lado do banco.

## Alternativa descartada por ora
Casos de uso devolverem objetos próprios (resultado com id, nome e e-mail), sem a web ver `User`.

## Revisar quando
- O `User` crescer e as mudanças começarem a vazar para a API.
