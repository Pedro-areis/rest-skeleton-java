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

O sistema deve permitir que o usuário consulte seus dados.

---

## RF04 — Atualizar Usuário

O sistema deve permitir que um usuário autenticado altere seus próprios
dados cadastrais.

O sistema deve impedir que um usuário altere os dados cadastrais de
outro usuário.

Os campos que podem ser alterados são:
- nome;
- e-mail, desde que não esteja sendo utilizado por outro usuário;
- senha;
- data de nascimento.

---

## RF05 — Excluir um Usuário

O sistema deve permitir que o usuário exclua seu cadastro no sistema. O administrador também pode excluir os dados do usuário.

---

## RNF01 — Persistência

Os dados devem ser persistidos em PostgreSQL.

## RNF02 — API

A aplicação deve disponibilizar uma API REST.

## RNF03 — Testes

As regras de negócio devem possuir testes automatizados.