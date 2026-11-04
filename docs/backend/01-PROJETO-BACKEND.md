# Projeto Cartões de Ônibus: Back-end

> **Para a IA:** este documento descreve o back-end do projeto. Leia também `docs/REGRAS-IA.md`, cujas regras são obrigatórias,
> e `docs/backend/02-PASSO-A-PASSO-BACKEND.md`, que mostra onde estamos.
> O back-end é desenvolvido no **IntelliJ IDEA** e o front-end, no VS Code. Os dois ficam no mesmo repositório (monorepo).

---

## 1. O desafio (resumo)

Desafio técnico para a vaga de Analista. O avaliador valoriza **código bem testado**. **Entrega: quarta, 04/11/2026.**

Sistema de cadastro de **usuários** e seus **cartões de ônibus**:

| Usuário | Cartão |
|---|---|
| Listar todos | Listar todos |
| Incluir | Adicionar a um usuário |
| Alterar | Remover de um usuário |
| Remover | Ativar / inativar |

- **Usuário:** nome, email, senha e lista de cartões (0 ou mais)
- **Cartão:** numeroCartao (número), nome, status (boolean), tipoCartao (`COMUM`, `ESTUDANTE`, `TRABALHADOR`)

### Escopo por prioridade

| Prioridade | O que entra |
|---|---|
| **1. Obrigatórios** | Java 8 ou mais novo, Spring Boot, Maven, JPA, camadas, banco SQL, Angular 4 ou mais novo e repositório Git online |
| **2. Extras definidos** | Docker e pipeline de CI (GitHub Actions) |
| **3. Diferenciais de baixo custo** | Migrações, Swagger, nativeQuery, sem Lombok, DTO, design patterns e layout responsivo |
| **4. Se der tempo** | Login e perfis com permissão |
| **5. Se ainda der tempo** | Microsserviço de filas com RabbitMQ, depois deploy em VPS |

---

## 2. Arquitetura (monólito modular)

```
Angular ──HTTP──► backend :8080 ──► PostgreSQL
                  ├── usuario/
                  └── cartao/
```

É **uma aplicação e um banco**, com os módulos `usuario` e `cartao` separados por dentro. Cada módulo mantém as camadas controller → service → repository.

### Regras entre os módulos

1. **Um módulo não usa o repository, o service nem a entidade do outro.** Eles conversam por interfaces públicas, no pacote `api/` de cada módulo.
2. **Não há relação JPA entre `Usuario` e `Cartao`.** O cartão guarda só o `usuarioId`. A chave estrangeira existe apenas no banco, para proteger os dados.
3. **Remover um usuário dispara um evento do Spring.** O módulo de usuário não sabe o que acontece com os cartões.

### Contratos entre os módulos

| Contrato | Implementação | Quem usa | Para quê |
|---|---|---|---|
| `CartaoConsulta` | `CartaoConsultaImpl`, usando `CartaoRepository` | `UsuarioService` | Preencher `cartoes` na resposta do usuário |
| `UsuarioConsulta` | `UsuarioConsultaImpl`, usando `UsuarioRepository` | `CartaoService` | Confirmar que o usuário existe ao adicionar um cartão |

> **Importante:** as implementações usam o repository, e não o service do próprio módulo. Se `CartaoService` e `UsuarioService` dependessem um do outro, o Spring Boot não iniciaria, porque recusa dependências circulares por padrão.

### Requisito "Usuário tem uma lista de cartões"

1. O front chama `GET /api/usuarios`.
2. O `UsuarioService` busca os usuários.
3. Ele pede os cartões a `CartaoConsulta.listarPorUsuarios(ids)`, em **uma única consulta**, para evitar o problema de N+1.
4. O mapper monta o `UsuarioResponse` com `cartoes: [...]`.

### Remover um usuário

1. O `UsuarioService` publica `UsuarioRemovidoEvent` **antes** de excluir o usuário.
2. Um `@EventListener` do módulo de cartão, **síncrono e na mesma transação**, remove os cartões com uma `nativeQuery`.
3. O `UsuarioService` exclui o usuário. A ordem respeita a chave estrangeira, e uma falha desfaz tudo.

---

## 3. Stack

| Item | Escolha | Observação |
|---|---|---|
| Linguagem | **Java 21** (LTS) | Já instalado |
| Framework | **Spring Boot** | Usar a versão estável do Initializr e mantê-la fixa até a entrega |
| Build | **Maven** (com Maven Wrapper `mvnw`) | Um único `pom.xml` |
| Persistência | **Spring Data JPA** (Hibernate + JDBC) | Inclui `nativeQuery` |
| Banco | **PostgreSQL** | Rodando em Docker |
| Migrações | **Flyway** | `src/main/resources/db/migration` |
| Senha | **BCrypt** (`spring-security-crypto`) | Sem o starter de segurança até a etapa de login |
| Documentação | **springdoc-openapi** (Swagger UI) | `/swagger-ui.html`; escolher a versão compatível com o Boot |
| Validação | **Bean Validation** (`@NotBlank`, `@Email`...) | |
| Testes | **JUnit 5, Mockito, MockMvc, Testcontainers** | Cobertura com **JaCoCo** |
| Container | **Docker + Docker Compose** | |
| CI | **GitHub Actions** | |
| Lombok | ❌ **Não usar** | Escrever getters, setters e construtores (o IntelliJ gera com Alt+Insert) |
| Login (se der tempo) | **Spring Security + JJWT** | Só na Etapa 10 |
| Filas (se der tempo) | **RabbitMQ** (Spring AMQP) | Só na Etapa 11 |

---

## 4. Estrutura de pastas

```
backend/
├── pom.xml
├── Dockerfile
└── src/
    ├── main/java/br/com/desafio/
    │   ├── usuario/
    │   │   ├── api/            ← contrato público do módulo (UsuarioConsulta, UsuarioRemovidoEvent)
    │   │   ├── controller/     ← recebe HTTP, devolve DTO (sem regra de negócio)
    │   │   ├── service/        ← regras de negócio
    │   │   ├── repository/     ← acesso ao banco (JPA + nativeQuery)
    │   │   ├── domain/         ← entidade JPA
    │   │   ├── dto/            ← entrada e saída da API (records)
    │   │   └── mapper/         ← converte Entidade ↔ DTO
    │   ├── cartao/             ← mesma estrutura (api/ tem CartaoConsulta)
    │   └── shared/             ← erros padrão e configurações (Swagger, CORS)
    ├── main/resources/
    │   ├── application.yml
    │   └── db/migration/       ← V1__criar_tabela_usuario.sql ...
    └── test/java/...           ← testes unitários e de integração
```

---

## 5. Modelo de dados

```sql
usuario (
  id          BIGSERIAL PRIMARY KEY,
  nome        VARCHAR(120) NOT NULL,
  email       VARCHAR(160) NOT NULL UNIQUE,
  senha       VARCHAR(100) NOT NULL,       -- hash BCrypt, nunca texto puro
  criado_em   TIMESTAMP    NOT NULL
)

cartao (
  id             BIGSERIAL PRIMARY KEY,
  numero_cartao  BIGINT       NOT NULL UNIQUE,
  nome           VARCHAR(120) NOT NULL,
  status         BOOLEAN      NOT NULL DEFAULT TRUE,   -- true = ativo
  tipo_cartao    VARCHAR(20)  NOT NULL,                -- COMUM | ESTUDANTE | TRABALHADOR
  usuario_id     BIGINT       NOT NULL REFERENCES usuario (id)
)
```

Se o login for feito, uma nova migração adiciona `perfil VARCHAR(20) NOT NULL DEFAULT 'USUARIO'` à tabela `usuario`.

---

## 6. Endpoints (`http://host:8080`)

### Usuários
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/usuarios` | Lista todos, cada um com seus cartões |
| GET | `/api/usuarios/{id}` | Busca um usuário com seus cartões |
| POST | `/api/usuarios` | Inclui um usuário (senha obrigatória) |
| PUT | `/api/usuarios/{id}` | Altera um usuário (senha opcional: vazia mantém a atual) |
| DELETE | `/api/usuarios/{id}` | Remove um usuário e seus cartões |

### Cartões
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/cartoes` | Lista todos (filtro opcional `?usuarioId=`) |
| POST | `/api/cartoes` | Adiciona um cartão a um usuário |
| DELETE | `/api/cartoes/{id}` | Remove um cartão |
| PATCH | `/api/cartoes/{id}/status` | Ativa ou inativa (`{ "status": true }`) |

**DTOs de usuário:** `UsuarioCreateRequest` (senha obrigatória), `UsuarioUpdateRequest` (senha opcional) e `UsuarioResponse`.
**A senha nunca aparece** em nenhum DTO de resposta.
**Erros** seguem um formato padrão: `{ timestamp, status, erro, mensagem, caminho }`.

### Se o login for feito (Etapa 10)

| Rota | Permissão |
|---|---|
| `POST /api/auth/login` | Público |
| Swagger (`/swagger-ui/**`, `/v3/api-docs/**`) | Público |
| `/api/usuarios/**` | ADMIN |
| `GET /api/cartoes` | ADMIN vê todos; USUARIO vê **só os próprios**, e o filtro `usuarioId` é ignorado para ele |
| `POST`, `DELETE` e `PATCH` em `/api/cartoes` | ADMIN |

- O JWT leva o `id` e o `perfil` do usuário.
- `perfil` passa a fazer parte dos DTOs de criação, de alteração e de resposta do usuário.
- O ADMIN inicial é criado na inicialização, por um `ApplicationRunner`, com `ADMIN_EMAIL` e `ADMIN_SENHA` vindos do ambiente.

---

## 7. Design patterns usados (para citar no README)

| Pattern | Onde |
|---|---|
| **Layered Architecture** | controller → service → repository |
| **DTO** | entrada e saída da API, sem expor a entidade |
| **Mapper** | conversão entre entidade e DTO |
| **Repository** | Spring Data JPA |
| **Observer** | `UsuarioRemovidoEvent` com `@EventListener` |
| **Facade** | `CartaoConsulta` e `UsuarioConsulta` entre os módulos |
| **Builder** | montagem de objetos nos testes |

---

## 8. Evolução: microsserviço de filas (se der tempo)

Um novo serviço, `notificacao-service`, fica em uma pasta própria e tem banco próprio (`notificacao_db`). O monólito continua existindo.

```
backend ──publica──► RabbitMQ ──consome──► notificacao-service ──► notificacao_db
```

| Evento | O que o serviço faz |
|---|---|
| `cartao.status-alterado` | Registra a notificação da mudança de status |
| `usuario.removido` | Registra a notificação da remoção |

- O `backend` publica **depois do commit** (`@TransactionalEventListener(AFTER_COMMIT)`), para não anunciar algo que foi desfeito.
- A remoção dos cartões continua síncrona, pelo evento interno do Spring.
- As filas são duráveis, com fila de mensagens com falha (DLQ), e o consumidor trata mensagens repetidas.
- **No README:** explicar que o sistema é um monólito modular com um microsserviço assíncrono, e não uma arquitetura toda em microsserviços.

---

## 9. Portas e ambiente

| Serviço | Porta |
|---|---|
| backend | 8080 |
| PostgreSQL | 5432 |
| frontend | 4200 (dev) / 80 (Docker com nginx) |
| notificacao-service (se der tempo) | 8081 |
| RabbitMQ (se der tempo) | 5672 (painel: 15672) |

As configurações sensíveis (senha do banco, segredo do JWT, senha do ADMIN) ficam em um `.env` na raiz, **fora do Git**. Um `.env.example` vai para o Git como modelo.
