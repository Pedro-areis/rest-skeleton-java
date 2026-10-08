# Requisitos

## RF01 — Criar um usuário

O sistema deve permitir que um usuário se cadastre.

Dados obrigatórios:

- nome
- data de nascimento
- e-mail
- senha

Regras da senha:

- mínimo de 8 caracteres
- máximo de 72 bytes (limite do BCrypt, ver ADR 003)
- não exigir letra maiúscula, números ou símbolos

---

## RF02 — Autenticar Usuário

O sistema deve permitir que o usuário faça o login com as seguintes informações:

- e-mail
- senha

O login devolve um access token e um refresh token. O sistema deve permitir obter um novo
access token a partir de um refresh token válido.

---

## RF03 — Consultar os próprios dados

O sistema deve permitir que o usuário autenticado consulte seus próprios dados.

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

O sistema deve permitir que o usuário autenticado exclua o próprio cadastro.

O sistema não possui administrador nem papéis (ver "Fora do escopo" em `PROJECT.md`).

---

## RNF01 — Persistência

Os dados devem ser persistidos em PostgreSQL.

## RNF02 — API

A aplicação deve disponibilizar uma API REST.

## RNF03 — Testes

As regras de negócio devem possuir testes automatizados.