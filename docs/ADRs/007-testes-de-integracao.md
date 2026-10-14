# 007 — Testes de integração: PostgreSQL em container, MockMvc e limpeza por teste

**Data:** 14/10/2026
**Status:** Decidido e implementado

## Contexto
Os testes de `user` e `auth` usam fakes, mocks e `@WebMvcTest` com `TokenPort` simulado (ADR 006). Nada prova a aplicação inteira junta: segurança com o `JwtTokenAdapter` real, JPA, Flyway e PostgreSQL. Também faltava conferir que o `save` mantém o id gerado pelo domínio e que a violação `23505` real vira 409.

## Decisão
- **Banco:** PostgreSQL real em container (Testcontainers, imagem `postgres:16-alpine`). O container é estático (`PostgresContainerConfig`), sobe uma vez por execução da suíte e é compartilhado por todas as classes. O `@ServiceConnection` aponta o datasource para ele. O banco local do desenvolvedor não é usado nem alterado. H2 foi descartado.
- **Chamadas:** `@SpringBootTest` + `@AutoConfigureMockMvc`, aplicação completa e sem porta HTTP.
- **Isolamento:** `TRUNCATE TABLE users` no `@BeforeEach` da classe base (`AbstractIntegrationTest`), com commits reais.
- **Execução:** Surefire único. As classes `*IntegrationTest` rodam no `./mvnw test`, que passa a exigir o Docker em execução. O `contextLoads` usa o mesmo container.
- **Escopo:** fluxo completo (cadastro, login, `/me`, `PATCH`, renovação, `DELETE`); persistência (id do domínio mantido, hash BCrypt, `created_at`, e-mail duplicado real, e-mail de outro usuário no `PATCH`); erros (401 sem token, token adulterado, tipo de token trocado, usuário excluído, login com a mesma mensagem para senha errada e e-mail inexistente; 400 de validação).
- **Encoding:** `application.properties` convertido para UTF-8. O arquivo em ISO-8859-1 quebrava a cópia de recursos do Maven, e os testes de integração precisam dele copiado.

## Trade-offs
**Ganhos**
- O mesmo banco da produção: migrações Flyway, `ddl-auto=validate` e o código `23505` são exercitados de verdade.
- Cada execução começa limpa e não toca nos dados locais.
- A lacuna do ADR 006 (teste com `TokenPort` simulado) é fechada, pois estes testes usam o `JwtTokenAdapter` real.

**Custos**
- `./mvnw test` exige o Docker Desktop em execução. Sem ele, os testes de integração falham.
- A primeira execução baixa a imagem do PostgreSQL e o `./mvnw test` fica mais lento (a primeira classe de integração levou cerca de 36 s, o resto é rápido).
- O MockMvc não passa pelo Tomcat: não prova a camada de rede nem a serialização final por HTTP.
- Limpar a tabela com `TRUNCATE` impede rodar classes de integração em paralelo contra o mesmo banco.
- Três dependências de teste a mais no `pom.xml`.

## Alternativas descartadas
- **H2 em modo PostgreSQL:** diferenças de SQL, de tipos e do código de violação podem esconder erros que só apareceriam em produção.
- **Banco de teste local (`rest_skeleton_test`):** sem Docker, mas depende de provisionar o banco à mão e no CI.
- **Porta real (`RANDOM_PORT`):** cobre o Tomcat, mas é mais lento e verboso, com ganho pequeno para este esqueleto.
- **`@Transactional` com rollback:** pode esconder falhas que só ocorrem no commit ou no `flush`.
- **E-mail único por teste, sem limpeza:** os dados se acumulam e testes de contagem ficam frágeis.
- **Failsafe (`*IT` no `verify`):** mantém o `test` rápido e sem Docker, mas exige configuração extra e dois comandos.

## Revisar quando
- A suíte de integração ficar lenta a ponto de atrapalhar (migrar para o Failsafe).
- O CI for configurado (garantir Docker no runner).
- Surgir outra tabela (incluir no `TRUNCATE`).
