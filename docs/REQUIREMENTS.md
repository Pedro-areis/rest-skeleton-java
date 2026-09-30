# Requisitos

## RF01 — Criar um usuário

O sistema deve permitir que um usuário se cadastre.

Dados obrigatórios:

- nome
- data de nascimento
- e-mail
- senha

---

## RF02 — Autenticar Usuário

O sistema deve permitir que o usuário faça o login com as seguintes informações:

- e-mail
- senha

---

## RF03 — Consultar usuários pelo e-mail

O sistema deve permitir que um administrador consulte os usuários daquele sistema pelo e-mail.

---

## RF04 — Listar Usuários

O sistema deve permitir listar os usuários cadastrados. Apenas o administrador terá acesso a esse endpoint.

---

## RF05 — Atualizar Usuário

O sistema deve permitir alterar:

- nome
- e-mail, desde que não esteja sendo utilizado por outro usuário
- senha
- data de nascimento

---

## RF06 — Excluir um Usuário

O sistema deve permitir que o usuário exclua seu cadastro no sistema. O administrador também pode excluir os dados do usuário.

---

## RNF01 — Persistência

Os dados devem ser persistidos em PostgreSQL.

## RNF02 — API

A aplicação deve disponibilizar uma API REST.

## RNF03 — Testes

As regras de negócio devem possuir testes automatizados.