# 009 — Hash de senha com BCrypt

**Data:** 30/09/2026
**Status:** Decidido

## Contexto
Hoje a senha é gravada em texto puro, apesar de a coluna se chamar `password_hash`.

## Decisão
Usar BCrypt (`BCryptPasswordEncoder`).

## Trade-offs
**Ganhos**
- Padrão histórico do ecossistema Spring, simples de usar e bem conhecido.

**Custos**
- Argon2 é mais moderno, mas exige biblioteca adicional (BouncyCastle) e mais complexidade.
- BCrypt é lento de propósito, o que pesa em testes que o usam de verdade (por isso, ver a decisão 010).

## Revisar quando
- Um projeto derivado exigir algoritmo mais moderno. Com a decisão 010, basta trocar o adaptador.
