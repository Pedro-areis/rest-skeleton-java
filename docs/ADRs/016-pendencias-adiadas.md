# 016 — Pendências adiadas

**Data:** 30/09/2026
**Status:** Adiado por decisão

| Tema | Situação |
|---|---|
| Semântica do PATCH | String vazia deve ser tratada, mas fica para depois. Hoje `null` e vazio são ignorados |
| Docker e CI | `Dockerfile`, `docker-compose.yml` e `deploy.yml` estão vazios. Verificar também se `deploy.yml` combina com "deploy fora do escopo" |
| Configuração por ambiente | Onde ficam as configurações do banco |
| `.gitignore` | Contém marcadores de conflito de merge (`<<<<<<<`, `=======`, `>>>>>>>`) |

## Custo de adiar
- O conflito no `.gitignore` pode fazer regras de ignore não funcionarem e arquivos indesejados serem versionados.

## Revisar quando
- Antes de subir o projeto no GitHub como esqueleto para reutilização.
