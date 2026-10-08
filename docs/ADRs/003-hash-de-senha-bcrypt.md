# 003 — Hash de senha com BCrypt, atrás de uma porta

**Data:** 30/09/2026 (atualizado na Task 5)
**Status:** Decidido e implementado

## Contexto
A senha era gravada em texto puro, apesar de a coluna se chamar `password_hash`. O domínio não pode depender de biblioteca de segurança (ver ADR 001).

## Decisão
- Usar BCrypt (`BCryptPasswordEncoder`).
- O domínio conhece apenas a porta `PasswordHasherPort` (`hash` e `matches`), que mora em `user/ports/outbound`. O algoritmo fica no adaptador `BCryptPasswordHasherAdapter`.
- O `UserService` gera o hash depois das verificações baratas, porque o BCrypt custa cerca de 100 ms.
- O BCrypt só considera os primeiros 72 **bytes** da senha. O adaptador rejeita senhas acima disso com `InvalidPasswordException` (400), contando bytes e não caracteres.
- Os testes de domínio usam um `FakePasswordHasher`; só o teste do adaptador usa o BCrypt de verdade.

## Trade-offs
**Ganhos**
- Padrão histórico do ecossistema Spring, simples de usar e bem conhecido.
- Trocar o algoritmo é escrever outro adaptador, sem mexer no domínio.

**Custos**
- Argon2 é mais moderno, mas exige biblioteca adicional (BouncyCastle) e mais complexidade.
- BCrypt é lento de propósito, o que pesa em testes que o usam de verdade (por isso o fake nos testes de domínio).
- Migrar para Argon2 mantendo usuários antigos pediria um hasher "delegante" (adiado, ver `TASKS.md`).

## Revisar quando
- Um projeto derivado exigir algoritmo mais moderno.
