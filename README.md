# Cartões de Ônibus

[![Backend CI](https://github.com/joao4692/desafio-analista-tecnico/actions/workflows/backend-ci.yml/badge.svg?branch=main)](https://github.com/joao4692/desafio-analista-tecnico/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/joao4692/desafio-analista-tecnico/actions/workflows/frontend-ci.yml/badge.svg?branch=main)](https://github.com/joao4692/desafio-analista-tecnico/actions/workflows/frontend-ci.yml)

Cadastro de usuários e dos seus cartões de ônibus, com API em **Spring Boot** e interface em **Angular**, feito para o desafio técnico de Analista.

- **Usuários:** listar, incluir, alterar e remover. Cada usuário tem uma lista de 0 ou mais cartões.
- **Cartões:** adicionar a um usuário, remover, listar (com filtro por usuário) e ativar ou inativar.
- **Cartão:** `numeroCartao`, `nome`, `status` e `tipoCartao` (`COMUM`, `ESTUDANTE` ou `TRABALHADOR`).

## Como rodar

Só é preciso ter o **Docker** (com o Docker Compose v2).

```bash
git clone https://github.com/joao4692/desafio-analista-tecnico.git
cd desafio-analista-tecnico

# Variáveis de ambiente: o modelo já funciona, mas troque a senha do banco se quiser
cp .env.example .env

# Sobe o banco, a API e o front
docker compose up --build
```

| O quê | Endereço |
|---|---|
| Aplicação (front) | http://localhost |
| Documentação da API (Swagger) | http://localhost:8080/swagger-ui.html |
| API | http://localhost:8080/api |

O banco começa vazio: cadastre um usuário e depois adicione cartões a ele.

**Portas usadas:** 80 (front), 8080 (API) e 5432 (PostgreSQL). Se a porta 80 estiver ocupada, defina `FRONTEND_PORTA=8000` no `.env`. Se você já tiver um PostgreSQL rodando na 5432, pare-o antes de subir o compose.

Para desligar: `docker compose down` (os dados ficam no volume). Para apagar também os dados: `docker compose down -v`.

## Testes

Todos os testes rodam no **GitHub Actions** a cada pull request, e a `main` só aceita merge com os dois pipelines verdes.

| Parte | Quantidade | Tipos |
|---|---|---|
| Back-end | 97 testes, cobertura de 99% das instruções e 100% dos desvios (JaCoCo) | Unitários (Mockito), de controller (MockMvc) e de integração com PostgreSQL real (Testcontainers) |
| Front-end | 61 testes | Services (HttpTestingController), componentes, formulários, interceptor e alertas (Vitest) |

Para rodar localmente:

```bash
# Back-end: precisa de Java 21 e do Docker ligado (o Testcontainers sobe um PostgreSQL)
cd backend
./mvnw verify
# Relatório de cobertura: backend/target/site/jacoco/index.html

# Front-end: precisa de Node 22
cd frontend
npm ci
npm run lint
npm test -- --watch=false
```

## Desenvolvimento sem Docker para o código

Útil para mexer no código com recarga automática. O banco continua no Docker.

```bash
cp .env.example .env
docker compose up -d postgres

# API em http://localhost:8080 (lê o .env da raiz)
cd backend && ./mvnw spring-boot:run

# Front em http://localhost:4200, com proxy de /api para a API
cd frontend && npm ci && npm start
```

O `npm start` também aceita conexões da rede local, para testar no celular pelo IP do computador (ex.: `http://192.168.0.10:4200`).

## Arquitetura

```
Navegador ──► nginx (front Angular) ──/api──► backend Spring Boot ──► PostgreSQL
                                              ├── usuario/
                                              └── cartao/
```

O back-end é um **monólito modular**: uma aplicação e um banco, com os módulos `usuario` e `cartao` separados por dentro. Cada módulo tem as suas camadas (`controller` → `service` → `repository`) e só expõe ao outro o pacote `api/`.

| Regra entre os módulos | Como é feito |
|---|---|
| Um módulo não usa o repository, o service nem a entidade do outro | Contratos `UsuarioConsulta` e `CartaoConsulta`, no pacote `api/` de cada módulo |
| Não há relação JPA entre `Usuario` e `Cartao` | O cartão guarda só o `usuarioId`; a chave estrangeira existe no banco para proteger os dados |
| Remover um usuário remove os cartões dele | O `UsuarioService` publica `UsuarioRemovidoEvent`, e um `@EventListener` do módulo de cartão apaga os cartões **na mesma transação**: se algo falhar, nada é removido |
| A lista de usuários traz os cartões de cada um | Uma única consulta busca os cartões de todos os usuários da página (sem N+1) |

Com essa separação, cada módulo pode virar um microsserviço depois: os contratos viram chamadas HTTP e o evento interno vira uma mensagem numa fila.

O front fala só com `/api`. No Docker, o nginx repassa essas chamadas para a API; no desenvolvimento, o proxy do `ng serve` faz o mesmo. Por isso o front não depende de CORS e funciona igual no computador e no celular.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/usuarios` | Lista todos os usuários, cada um com seus cartões |
| GET | `/api/usuarios/{id}` | Busca um usuário com seus cartões |
| POST | `/api/usuarios` | Inclui um usuário (senha obrigatória) |
| PUT | `/api/usuarios/{id}` | Altera um usuário (senha vazia mantém a atual) |
| DELETE | `/api/usuarios/{id}` | Remove o usuário e os cartões dele |
| GET | `/api/cartoes?usuarioId=` | Lista os cartões, com filtro opcional por usuário |
| POST | `/api/cartoes` | Adiciona um cartão a um usuário |
| DELETE | `/api/cartoes/{id}` | Remove um cartão |
| PATCH | `/api/cartoes/{id}/status` | Ativa ou inativa um cartão (`{ "status": true }`) |

- A senha é guardada com **BCrypt** e nunca aparece nas respostas.
- E-mail e número do cartão são únicos: repetir dá `409` com a mensagem do motivo.
- Todos os erros seguem o formato `{ timestamp, status, erro, mensagem, caminho }`, com mensagens em português.

## Decisões técnicas

| Tema | Decisão |
|---|---|
| Banco e migrações | PostgreSQL com **Flyway**; o Hibernate só valida o esquema (`ddl-auto: validate`) |
| Consultas | Spring Data JPA, com **nativeQuery** para buscar cartões de vários usuários de uma vez e para apagar os cartões de um usuário num único `DELETE` |
| DTOs | `record`s de entrada e saída separados por operação (`UsuarioCreateRequest`, `UsuarioUpdateRequest`, `UsuarioResponse`...); as entidades nunca saem da API |
| Lombok | Não usado: getters, setters e construtores escritos à mão |
| Validação | Bean Validation no back e as mesmas regras nos formulários do front |
| Segredos | Senha do banco só no `.env`, que fica fora do Git |
| Imagens Docker | Multi-stage nas duas partes, rodando sem usuário root |
| Front | Angular 22 com standalone components e signals, Reactive Forms e Bootstrap 5 |
| Responsivo | **Mobile-first**: cards e menu colapsado no celular; tabelas a partir de 768 px; área de toque de 44 px |

## Design patterns

| Pattern | Onde |
|---|---|
| Layered Architecture | `controller` → `service` → `repository` em cada módulo |
| DTO | Entrada e saída da API, sem expor as entidades |
| Mapper | `UsuarioMapper` e `CartaoMapper` |
| Repository | Spring Data JPA |
| Facade | `UsuarioConsulta` e `CartaoConsulta` entre os módulos |
| Observer | `UsuarioRemovidoEvent` com `@EventListener` |
| Interceptor | `erroInterceptor` no front: toda falha da API vira uma mensagem na tela |

## Requisitos do desafio

| Requisito | Situação |
|---|---|
| Java 8 ou mais novo | ✅ Java 21 |
| Spring Boot | ✅ 4.1 |
| Build com Maven | ✅ Com Maven Wrapper |
| Persistência com JDBC ou JPA | ✅ Spring Data JPA |
| Aplicação em camadas | ✅ |
| Banco relacional | ✅ PostgreSQL |
| Angular 4 ou mais novo | ✅ Angular 22 |
| Repositório Git online | ✅ GitHub |
| Migrações | ✅ Flyway |
| Swagger | ✅ springdoc-openapi |
| nativeQuery | ✅ |
| Sem Lombok | ✅ |
| DTO | ✅ |
| Design patterns | ✅ Ver a tabela acima |
| Design responsivo | ✅ Bootstrap 5, mobile-first |
| Microsserviços | ⏳ Não feito: monólito modular, preparado para a divisão |
| Login e perfis com permissão | ⏳ Não feito |

## Estrutura

```
├── backend/                 API Spring Boot
│   └── src/main/java/br/com/desafio/
│       ├── usuario/         api, controller, service, repository, domain, dto, mapper
│       ├── cartao/          mesma estrutura
│       └── shared/          erros padrão e configurações (Swagger, CORS, BCrypt)
├── frontend/                Angular
│   └── src/app/
│       ├── core/            navbar e interceptor de erros
│       ├── shared/          alerta e indicador de carregamento
│       ├── models/          interfaces da API
│       └── features/        telas de usuários e de cartões
├── .github/workflows/       CI do back e do front
└── docker-compose.yml       postgres, backend e frontend
```
