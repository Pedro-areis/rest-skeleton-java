# 001 — Arquitetura hexagonal, organizada por funcionalidade

**Data:** 30/09/2026
**Status:** Existente no código (não documentada antes) e mantida

## Contexto
O projeto nasceu para aprender arquitetura hexagonal (Ports & Adapters). O código é organizado por funcionalidade (`user`) e, dentro dela, por camada: `domain`, `ports`, `adapters`. O domínio não tem anotações do Spring; o `UserService` é registrado como bean manualmente em `UserBeanConfig`.

## Decisão
Manter a hexagonal. Ela não é obrigatória para sempre: quem clonar o esqueleto pode decidir, projeto a projeto, se continua com ela.

## Trade-offs
**Ganhos**
- Trocar banco ou tecnologia vira escrever um adaptador novo.
- Domínio testável sem framework.
- Objetivo de aprendizado atendido.

**Custos**
- Mais interfaces e mais código do que um CRUD simples exigiria.
- Registro manual de beans em `UserBeanConfig`.

## Revisar quando
- O custo da arquitetura superar o benefício em um projeto real.
