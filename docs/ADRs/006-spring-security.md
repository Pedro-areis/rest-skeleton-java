# 006 — Spring Security: filtro de token, rotas públicas por prefixo e erros 401/403

**Data:** 13/10/2026
**Status:** Decidido e implementado

## Contexto
As rotas precisam exigir um access token (ADR 002), exceto cadastro, login e renovação. O Spring Security já está no classpath e, sem configuração, protege tudo com Basic auth. É preciso decidir onde mora o filtro que lê o token, como as rotas públicas são declaradas, como 401 e 403 viram o `ErrorResponse` do ADR 005, o que fazer com sessão, CSRF e CORS, e como testar.

## Decisão
- **Filtro:** `JwtAuthenticationFilter` (`OncePerRequestFilter`) em `auth/adapters/inbound`. Lê `Authorization: Bearer`, chama `TokenPort.parseAccessToken` e, se válido, coloca o **`UUID` do usuário como principal** (sem tipo próprio e sem papéis). A Task 8 lê o principal com `@AuthenticationPrincipal UUID`.
- **Token ausente ou inválido:** o filtro nunca responde. Segue a cadeia sem popular o contexto, e quem barra é `anyRequest().authenticated()`, com o `AuthenticationEntryPoint` devolvendo 401. A mensagem é a mesma para token ausente, expirado e adulterado.
- **Rotas públicas:** tudo sob `/api/v1/public/**` é liberado, o resto exige autenticação. Rotas públicas: `POST /api/v1/public/users/register`, `POST /api/v1/public/auth/login` e `POST /api/v1/public/auth/refresh`.
- **401 e 403:** um `AuthenticationEntryPoint` (401) e um `AccessDeniedHandler` (403) escrevem o `ErrorResponse` direto na resposta, com mensagens em português. Exceções lançadas dentro de um controller seguem outro caminho: o catch-all do `GlobalExceptionHandler` relança `AuthenticationException` e `AccessDeniedException` para o Spring Security tratar, em vez de devolver 500.
- **Usuário excluído com token ainda válido (Task 8):** o token é stateless e sobrevive ao `DELETE /me` até expirar. As rotas `/api/v1/users/me` (`GET`, `PATCH`, `DELETE`) tiram o id do principal, então todo `UserNotFoundException` vindo de `user` significa "token de usuário inexistente". O `UserExceptionHandler` o traduz para **401**, com a mesma mensagem do `AuthenticationEntryPoint`. O `GET /me` obtém isso com `findById(id).orElseThrow(UserNotFoundException::new)` no controller, e o `FindUserUseCase` continua devolvendo `Optional`.
- **Sessão, CSRF e CORS:** sem estado (`STATELESS`), CSRF, `formLogin` e `httpBasic` desligados. CORS configurável pela propriedade `api.cors.allowed-origins`, vazia por padrão (nenhuma origem liberada).
- **Testes:** `@WebMvcTest` com a cadeia de segurança real e o `TokenPort` simulado, sem banco.

## Trade-offs
**Ganhos**
- O controller de `user` não importa nada de `auth` (principal é um `UUID`), e a direção `auth → user` fica intacta.
- A regra de segurança é uma linha, e uma rota nova nasce protegida, não pública.
- Uma só origem para o 401 de rota protegida, e o formato de erro continua uniforme (ADR 005).
- Rota pública continua funcionando se o cliente mandar um token vencido junto.
- Ligar um frontend depois é só preencher a propriedade de CORS.

**Custos**
- O `UUID` solto como principal não carrega mais dados. Se um dia precisar de e-mail ou papéis, é preciso trocar o tipo do principal e os controllers.
- O prefixo `public` mistura "o que a rota é" com "quem pode chamar", e muda as URLs de cadastro, login e renovação.
- O cliente não distingue "token expirado, renove" de "nunca enviei token".
- `UserNotFoundException` deixa de ser 404 em `user`. Se um dia houver rota por id de outro usuário (por exemplo, um administrador), é preciso separar "sessão inválida" de "recurso não encontrado".
- O `ErrorResponse` é serializado à mão nos dois handlers, fora do MVC.
- CSRF desligado só é seguro porque não há cookie de sessão. Quem copiar esta configuração para um projeto com cookie precisa reavaliar.
- CORS mal configurado (por exemplo, `*`) abre a API a qualquer origem.
- O teste com `TokenPort` simulado não prova a integração com o `JwtTokenAdapter` real (isso ficou para a Task 9, fechado pelo ADR 007).

## Alternativas descartadas
- **Filtro:** principal como tipo próprio (o `UserController` importaria `auth`, ou o tipo iria para `shared`) e filtro em `config/` (`config/` deixaria de ser só fiação de beans).
- **Rotas públicas:** lista fixa de caminhos na configuração (caminho duplicado entre controller e configuração) e anotação própria (mais código, e uma anotação esquecida vira falha de segurança difícil de notar).
- **401/403:** delegar ao `HandlerExceptionResolver` e tratar no `GlobalExceptionHandler` (caminho indireto, e `shared/web` conheceria tipos do Spring Security) e responder só o status, sem corpo (quebra o formato único do ADR 005).
- **CSRF ligado:** exigiria token CSRF em todo `POST`, `PATCH` e `DELETE`, sem benefício numa API de token no header.
- **Filtro responde 401 ou traduz `InvalidTokenException`:** duplica a montagem do `ErrorResponse` e derruba rotas públicas quando o cliente manda um token vencido.
- **Testes:** só unitário do filtro, adiando a cadeia para a Task 9 (erro de configuração só apareceria lá), e `@WebMvcTest` com o `JwtTokenAdapter` real (sobrepõe o `JwtTokenAdapterTest`).

## Revisar quando
- Revogação e logout forem implementados, ou surgirem papéis e permissões (o principal deixa de ser um `UUID`).
- Um frontend de outra origem entrar no projeto.
- O número de rotas públicas crescer a ponto de o prefixo atrapalhar.
