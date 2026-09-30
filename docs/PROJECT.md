# Esqueleto CRUD

## Objetivo

Criar um esqueleto de uma API REST contendo um CRUD de usuários, autenticação baseada em token e uma estrutura inicial de testes, 
permitindo reutilizar essa base no início de novos projetos.

## Usuários

Desenvolvedores que estão iniciando um novo projeto.

## Problema

Desenvolvedores demoram para iniciar o projeto de fato (a solução) criando o CRUD de usuários que, na maioria dos projetos é o mesmo.

## Funcionalidades principais

- Criar um usuário
- Ler os usuários existentes (apenas ADM)
- Atualizar um usuário
- Excluir um usuário
- Autenticar usuários através de Tokens

## Fora do escopo

- Recuperação de senha por e-mail
- Confirmação de e-mail
- Login com Google, GitHub ou outros provedores
- Gerenciamento de permissões além do necessário para diferenciar usuário e administrador
- Integração com outros sistemas
- Interface gráfica / frontend
- Funcionalidades específicas de negócio
- Deploy em ambiente de produção

## Stack

- Java
- Spring Boot
- PostgreSQL
- Docker
- JUnit

## Restrições

- API REST
- Persistência relacional
- Código testável