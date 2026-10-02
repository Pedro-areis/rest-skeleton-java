# 002 — JWT com assinatura HS256 e access token de 1 hora

**Data:** 30/09/2026
**Status:** Decidido

## Contexto
A autenticação é baseada em token. JWT é o padrão do mercado. É preciso escolher o algoritmo de assinatura e a validade.

## Decisão
JWT assinado com HS256 (chave secreta única). Access token válido por 1 hora.

## Trade-offs
**Ganhos**
- Simples: uma chave só, sem par de chaves.

**Custos**
- Quem valida o token precisa conhecer a chave secreta (se houver vários serviços, todos a compartilham).
- RS256/ES256 (par público/privado) seria mais adequado para múltiplos serviços.

## Revisar quando
- O sistema passar a ter mais de um serviço validando tokens.
