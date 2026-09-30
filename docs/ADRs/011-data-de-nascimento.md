# 011 — Data de nascimento obrigatória e não futura

**Data:** 30/09/2026
**Status:** Decidido

## Contexto
RF01 e RF05 pedem data de nascimento, que ainda não existe em nenhuma camada.

## Decisão
Campo obrigatório (coluna `NOT NULL`), sem idade mínima. Apenas uma regra: a data não pode ser futura. Os dados atuais do banco (de teste) serão apagados antes da migração.

## Trade-offs
**Ganhos**
- Regra simples e suficiente para um esqueleto.
- `NOT NULL` garante consistência no banco.

**Custos**
- A migração falha se houver linhas antigas sem valor (por isso o banco será limpo antes).
- Sem idade mínima: cada projeto derivado decide se precisa.

## Revisar quando
- Um projeto derivado exigir idade mínima (por exemplo, maioridade).
