## Cartões de Ônibus

[![Backend CI](https://github.com/joao4692/desafio-analista-tecnico/actions/workflows/backend-ci.yml/badge.svg?branch=develop)](https://github.com/joao4692/desafio-analista-tecnico/actions/workflows/backend-ci.yml)


Sistema de cadastro de usuários e de seus cartões de ônibus, com API em Spring Boot e interface em Angular.

> Projeto em desenvolvimento.

## Stack

| Parte | Tecnologias |
|---|---|
| Back-end | Java 21, Spring Boot, Maven, Spring Data JPA, Flyway |
| Banco de dados | PostgreSQL |
| Testes | JUnit 5, Mockito, MockMvc, Testcontainers, JaCoCo |
| Front-end | Angular, Bootstrap 5 |
| Infraestrutura | Docker Compose, GitHub Actions |

## Como preparar o ambiente

Requisitos: Java 21 e Docker.

```bash
# Crie o .env a partir do modelo e defina a senha do banco
cp .env.example .env

# Suba o banco de dados
docker compose up -d

# Rode os testes (o Testcontainers usa o Docker)
cd backend
./mvnw verify
```
