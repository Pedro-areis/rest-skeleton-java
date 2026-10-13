# 002 — JWT com assinatura HS256 e access token de 1 hora

**Data:** 30/09/2026 (atualizado na Task 6)
**Status:** Decidido

## Contexto
A autenticação é baseada em token. JWT é o padrão do mercado. É preciso escolher o algoritmo de assinatura, a validade e como o token é montado, emitido e verificado.

## Decisão
JWT assinado com HS256 (chave secreta única). Access token válido por 1 hora e refresh token válido por 7 dias, apenas assinado (sem registro no servidor, portanto sem revogação).

Detalhes decididos na Task 6:
- **Biblioteca:** JJWT, com o módulo `jjwt-jackson`. Fica dentro do adaptador, atrás do `TokenPort`; o domínio não a conhece.
- **`sub`:** o id do usuário (UUID). O e-mail pode mudar pelo `PATCH`, o id não.
- **Tipo do token:** claim `type`, com os valores `access` e `refresh`. Cada operação exige o tipo certo: a autenticação das rotas aceita só `access`, e a renovação aceita só `refresh`.
- **Renovação:** recebe um refresh token e devolve **só um novo access token**. O refresh token continua o mesmo até expirar; depois de 7 dias do login, é preciso logar de novo.
- **Erro no login:** a mesma mensagem (401) para e-mail inexistente e senha errada, para não revelar quais e-mails estão cadastrados. A diferença de tempo de resposta entre os dois casos fica adiada (ver `TASKS.md`).
- **Segredo:** propriedade `api.security.token.secret`, lida da variável de ambiente `JWT_SECRET`, com um valor padrão para ambiente local. O HS256 exige chave de pelo menos 32 bytes.
- **Durações:** duas propriedades, uma para o access token (1 hora) e outra para o refresh token (7 dias).
- **Tempo nos testes:** o adaptador recebe um `java.time.Clock` no construtor. Em produção é o relógio do sistema; nos testes, um relógio fixo.

## Trade-offs
**Ganhos**
- Simples: uma chave só, sem par de chaves.
- Refresh token sem estado: nada para guardar no banco.
- `sub` com o id mantém o token válido para a mesma pessoa mesmo que ela troque de e-mail.
- O claim `type` impede que um refresh token (7 dias) seja usado como access token, e que um access token se renove sozinho.

**Custos**
- Sem revogação: um refresh token vazado vale até expirar (logout e revogação estão adiados).
- A renovação não estende a janela de 7 dias. Quem usa o sistema todo dia precisa logar de novo a cada semana.
- Quem valida o token precisa conhecer a chave secreta (se houver vários serviços, todos a compartilham).
- RS256/ES256 (par público/privado) seria mais adequado para múltiplos serviços.
- O valor padrão do segredo está no repositório. Em qualquer ambiente fora do local, `JWT_SECRET` deve ser definida, senão qualquer pessoa consegue forjar tokens.
- A checagem do `type` depende de cada operação lembrar de exigi-lo; o `TokenPort` concentra essa regra para reduzir o risco.
- O `jjwt-jackson` usa o Jackson 2, e o Spring Boot 4 usa o Jackson 3. Os dois convivem no classpath (pacotes diferentes), ao custo de duas versões de Jackson. Conferido com `dependency:tree`: o Jackson 3 (`tools.jackson`) vem do Boot, e o Jackson 2 (`com.fasterxml`) entra só pelo `jjwt-jackson`.

## Alternativas descartadas
- **Biblioteca:** Nimbus JOSE+JWT direto (API mais verbosa) e `NimbusJwtEncoder`/`Decoder` do Spring Security (acopla o adaptador a tipos do Spring e puxa o modelo de resource server).
- **`sub`:** e-mail (pode mudar e deixar tokens apontando para outra pessoa).
- **Tipo do token:** chaves distintas (dobra a configuração) e durações distintas sem marcador (não separa nada, os dois valem para qualquer operação).
- **Renovação:** par novo, com ou sem a expiração original (sem revogação, o refresh renovado indefinidamente é um risco).
- **Tempo nos testes:** `Thread.sleep` (lento e instável), instante como parâmetro do `TokenPort` e não testar a expiração.

## Revisar quando
- O sistema passar a ter mais de um serviço validando tokens.
- Revogação e logout forem implementados (a renovação pode passar a devolver um par novo, com rotação).
