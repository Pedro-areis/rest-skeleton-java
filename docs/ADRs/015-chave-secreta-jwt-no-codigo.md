# 015 — Chave secreta do JWT no código (temporário)

**Data:** 30/09/2026
**Status:** Provisório

## Contexto
A aplicação roda apenas localmente por enquanto, e a configuração por ambiente foi adiada.

## Decisão
Deixar a chave secreta no código/configuração local por ora.

## Trade-offs
**Ganhos**
- Menos trabalho agora; nada a configurar para rodar.

**Custos**
- Se o repositório for público, a chave fica pública. Quem clonar o esqueleto deve trocá-la.
- Nunca usar essa chave fora do ambiente local.

## Revisar quando
- Antes de qualquer deploy, ou ao tratar configuração por ambiente (variável de ambiente).
