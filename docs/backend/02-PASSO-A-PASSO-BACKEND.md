# Passo a passo: Back-end

> **Para a IA:** este é o roteiro e o status atual. Continue do primeiro passo `[a fazer]`.
> Siga `docs/REGRAS-IA.md`: **eu crio os arquivos**, você me diz o que criar e me manda o código comentado.
> A arquitetura e as decisões estão em `docs/backend/01-PROJETO-BACKEND.md`.

**Legenda:** `[concluído]` · `[a fazer]` · `[em andamento]` · ✅ = etapa ou passo concluído

**Ordem geral:** back obrigatório (Etapas 0 a 9) → front obrigatório (`docs/frontend/02-PASSO-A-PASSO-FRONTEND.md`) → Etapas opcionais (10 a 12) → Entrega (Etapa 13).

---

## ✅ Etapa 0: Ferramentas
Conferido em 01/11/2026 nesta máquina.

| Ferramenta | Necessária | Situação |
|---|---|---|
| Git | 2.x | ✅ 2.43.0 |
| Java (JDK) | 21 | ✅ OpenJDK 21.0.12 |
| Maven | 3.8 ou mais novo | ✅ 3.8.7 (vamos usar o `mvnw` do projeto) |
| Docker | qualquer recente | ✅ 29.9.0 (rodando sem sudo) |
| Docker Compose | v2 ou mais novo | ✅ v5.6.0 |
| IntelliJ IDEA | 2024 ou mais novo | ✅ 2026.2 |
| GitHub CLI (`gh`) | opcional | ✅ 2.45.0, logado na conta `joao4692` |
| Cliente de API | opcional | Usar o Swagger UI, Postman ou o HTTP Client do IntelliJ |

- ✅ Passo 0.1: Confirmar as ferramentas acima `[concluído]`
- ✅ Passo 0.2: Definir a conta do GitHub: `joao4692` `[concluído]`
- ✅ Passo 0.3: Abrir o IntelliJ e configurar o SDK do projeto como Java 21 (*File → Project Structure → SDK*) `[concluído]`

---

## ✅ Etapa 1: Repositório e branches
- ✅ Passo 1.1: `git init -b main` na raiz `Projeto_desafio_tecnico_Analista/` e conferir `git config user.name` e `user.email` `[concluído]`
- ✅ Passo 1.2: Decidir se o PDF do desafio e o `docs/REGRAS-IA.md` vão para o repositório público `[concluído]`
- ✅ Passo 1.3: Criar o `.gitignore` da raiz (IDEs, `.env`, arquivos do sistema operacional e o que for decidido no Passo 1.2) `[concluído]`
- ✅ Passo 1.4: Criar o `README.md` inicial (título, descrição e stack) `[concluído]`
- ✅ Passo 1.5: Criar o `.env.example` (modelo das variáveis de ambiente) `[concluído]`
- ✅ Passo 1.6: Primeiro commit `chore: estrutura inicial do repositório` `[concluído]`
- ✅ Passo 1.7: Criar o repositório no GitHub (público) e fazer o push da `main` `[concluído]`
- ✅ Passo 1.8: Criar a branch `develop` a partir da `main` e fazer o push `[concluído]`
- ✅ Passo 1.9: Proteger a `main` no GitHub (exigir PR e CI verde) `[concluído]` *(depois da Etapa 4)*

> A partir daqui, cada etapa é uma branch `feature/...` saindo da `develop`, com PR de volta para a `develop`.

---

## ✅ Etapa 2: Infraestrutura local (Docker)
Branch: `feature/infra-docker`
- ✅ Passo 2.1: Criar o `docker-compose.yml` na raiz com o **PostgreSQL** (banco criado por variável de ambiente) `[concluído]`
- ✅ Passo 2.2: Subir com `docker compose up -d` e testar a conexão com o banco `[concluído]`

---

## ✅ Etapa 3: Esqueleto do back-end
Branch: `feature/esqueleto-backend`
- ✅ Passo 3.1: Gerar `backend/` no Spring Initializr (Maven, Java 21; Web, JPA, PostgreSQL, Flyway, Validation e Testcontainers), **sem Spring Security** `[concluído]`
- ✅ Passo 3.2: Adicionar `spring-security-crypto` ao `pom.xml`, só para o BCrypt `[concluído]`
- ✅ Passo 3.3: Criar os pacotes `usuario/`, `cartao/` e `shared/` e abrir `backend/` no IntelliJ `[concluído]`
- ✅ Passo 3.4: Fazer o `contextLoads` gerado rodar com Postgres do Testcontainers, porque sem banco ele quebra o `verify` `[concluído]`
- ✅ Passo 3.5: Rodar `./mvnw verify` com sucesso `[concluído]`

---

## ✅ Etapa 4: Pipeline de CI (GitHub Actions)
Branch: `feature/ci`
- ✅ Passo 4.1: Criar `.github/workflows/backend-ci.yml` (Java 21, cache do Maven e `./mvnw verify`) `[concluído]`
- ✅ Passo 4.2: Adicionar o JaCoCo (relatório de cobertura) ao `pom.xml` `[concluído]`
- ✅ Passo 4.3: Abrir um PR e ver o pipeline rodar verde `[concluído]`
- ✅ Passo 4.4: Badge do CI no README `[concluído]`

---

## ✅ Etapa 5: Módulo de usuário (CRUD)
Branch: `feature/usuario-crud` (PR #5, merge na `develop` em 02/11/2026)
- ✅ Passo 5.1: `application.yml` (porta 8080, conexão com o banco via variáveis) `[concluído]`
- ✅ Passo 5.2: Migração `V1__criar_tabela_usuario.sql` (Flyway) `[concluído]`
- ✅ Passo 5.3: Entidade `Usuario`, sem Lombok `[concluído]`
- ✅ Passo 5.4: `UsuarioRepository` (JPA + uma `nativeQuery`) `[concluído]`
- ✅ Passo 5.5: DTOs (`UsuarioCreateRequest`, `UsuarioUpdateRequest`, `UsuarioResponse`) e `UsuarioMapper`, com teste do mapper `[concluído]`
- ✅ Passo 5.6: Exceções e `GlobalExceptionHandler` em `shared/` (formato de erro padrão), com teste `[concluído]`
- ✅ Passo 5.7: `UsuarioService` (regras: email único, senha com BCrypt, senha opcional ao alterar) e **teste unitário** com Mockito `[concluído]`
- ✅ Passo 5.8: `UsuarioController` (GET, POST, PUT, DELETE) e **teste com MockMvc** `[concluído]`
- ✅ Passo 5.9: **Teste de integração** com Testcontainers (Postgres real) `[concluído]`

---

## ✅ Etapa 6: Módulo de cartão (CRUD)
Branch: `feature/cartao-crud` (PR #6, merge na `develop` em 02/11/2026)
- ✅ Passo 6.1: Migração `V2__criar_tabela_cartao.sql`, com chave estrangeira para `usuario` `[concluído]`
- ✅ Passo 6.2: Enum `TipoCartao` e entidade `Cartao` (só com `usuarioId`, sem relação JPA) `[concluído]`
- ✅ Passo 6.3: `CartaoRepository` (com `nativeQuery`, ex.: listar por usuários e remover por usuário) `[concluído]`
- ✅ Passo 6.4: DTOs e mapper, com teste do mapper `[concluído]`
- ✅ Passo 6.5: Contrato `UsuarioConsulta` e `UsuarioConsultaImpl` (usa o `UsuarioRepository`) `[concluído]`
- ✅ Passo 6.6: `CartaoService` (adicionar, remover, listar, ativar/inativar, número único, usuário precisa existir) e testes unitários `[concluído]`
- ✅ Passo 6.7: `CartaoController` e testes com MockMvc `[concluído]`
- ✅ Passo 6.8: Teste de integração com Testcontainers `[concluído]`

---

## ✅ Etapa 7: Integração entre os módulos
Branch: `feature/integracao-modulos` (PR #7, merge na `develop` em 02/11/2026)
- ✅ Passo 7.1: Contrato `CartaoConsulta` e `CartaoConsultaImpl` (usa o `CartaoRepository`), com teste `[concluído]`
- ✅ Passo 7.2: `UsuarioResponse` com a lista de cartões, buscada em uma única consulta, e teste `[concluído]`
- ✅ Passo 7.3: `UsuarioRemovidoEvent`, publicado antes de excluir o usuário, e `@EventListener` no módulo de cartão, com teste `[concluído]`
- ✅ Passo 7.4: Teste de integração: remover um usuário remove os cartões, e uma falha desfaz tudo `[concluído]`

---

## ✅ Etapa 8: Swagger e CORS
Branch: `feature/swagger-cors` (PR #8, merge na `develop` em 02/11/2026)
- ✅ Passo 8.1: springdoc, com anotações `@Operation` nos controllers `[concluído]`
- ✅ Passo 8.2: CORS liberado para o front (`localhost:4200`), com teste `[concluído]`
- ✅ Passo 8.3: Mensagens de validação em português (idioma fixo `pt_BR`), com teste `[concluído]` *(acrescentado durante a etapa)*

---

## ✅ Etapa 9: Docker do back-end
Branch: `feature/docker-backend` (PR #9, merge na `develop` em 03/11/2026)
- ✅ Passo 9.1: `Dockerfile` multi-stage (build com Maven e imagem final enxuta com JRE) `[concluído]`
- ✅ Passo 9.2: Adicionar o `backend` ao `docker-compose.yml` `[concluído]`
- ✅ Passo 9.3: Subir com `docker compose up --build` e testar pelo Swagger `[concluído]`

> **Agora é a vez do front:** seguir o passo a passo do front-end até concluir a Etapa 7 dele. As etapas abaixo só começam depois disso.

---

## Etapa 10: Login e perfis (se der tempo)
Branch: `feature/seguranca-jwt`
- Passo 10.1: Adicionar o starter de segurança e o JJWT ao `pom.xml` `[a fazer]`
- Passo 10.2: Migração que adiciona `perfil`, enum `Perfil` e `perfil` nos DTOs de usuário `[a fazer]`
- Passo 10.3: `ApplicationRunner` que cria o ADMIN inicial com dados do ambiente, sem duplicar, e teste `[a fazer]`
- Passo 10.4: `POST /api/auth/login`, que gera o JWT com `id` e `perfil`, e teste `[a fazer]`
- Passo 10.5: Validação do JWT e regras por perfil, liberando o login, o Swagger e o preflight do CORS `[a fazer]`
- Passo 10.6: USUARIO lista só os próprios cartões `[a fazer]`
- Passo 10.7: Testes de segurança (sem token → 401, perfil errado → 403, USUARIO não vê cartão de outro) `[a fazer]`
- Passo 10.8: Botão "Authorize" com JWT no Swagger `[a fazer]`

---

## Etapa 11: Microsserviço de filas (se ainda der tempo)
Branch: `feature/microsservico-filas`

Só começa com o obrigatório entregue na `main`. Se não ficar pronto e com os testes passando, não entra.

- Passo 11.1: Adicionar o **RabbitMQ** ao `docker-compose.yml` `[a fazer]`
- Passo 11.2: Criar o projeto `notificacao-service/` com banco próprio e migração `[a fazer]`
- Passo 11.3: Configuração de exchange, filas e DLQ `[a fazer]`
- Passo 11.4: Publisher no `backend`, depois do commit (`cartao.status-alterado` e `usuario.removido`), e teste `[a fazer]`
- Passo 11.5: Consumer no `notificacao-service`, tratando mensagens repetidas, e teste `[a fazer]`
- Passo 11.6: Teste de integração com RabbitMQ do Testcontainers `[a fazer]`
- Passo 11.7: Adicionar o serviço ao CI e ao `docker-compose.yml` e atualizar o README `[a fazer]`

---

## Etapa 12: Deploy na VPS (se ainda der tempo)
Branch: `feature/cd-deploy`
- Passo 12.1: Preparar a VPS (Docker, usuário de deploy, firewall, chave SSH) `[a fazer]`
- Passo 12.2: Workflow de build e push das imagens no GHCR `[a fazer]`
- Passo 12.3: Workflow de deploy (SSH → `docker compose pull && up -d`), só na `main` `[a fazer]`
- Passo 12.4: Configurar os GitHub Secrets (host, chave SSH e `.env` de produção) `[a fazer]`

---

## Etapa 13: Entrega (até 04/11/2026)
- Passo 13.1: README final (como rodar, arquitetura, endpoints, decisões e como a divisão em microsserviços seria feita) `[a fazer]`
- Passo 13.2: Revisão final: testes verdes, cobertura e Swagger funcionando `[a fazer]`
- Passo 13.3: Conferir todos os requisitos obrigatórios do PDF, um a um `[a fazer]`
- Passo 13.4: Merge `develop` → `main` e tag `v1.0.0` `[a fazer]`
