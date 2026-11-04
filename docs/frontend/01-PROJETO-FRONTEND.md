# Projeto Cartões de Ônibus: Front-end

> **Para a IA:** este documento descreve o front-end do projeto. Leia também `docs/REGRAS-IA.md`, cujas regras são obrigatórias,
> e `docs/frontend/02-PASSO-A-PASSO-FRONTEND.md`, que mostra onde estamos.
> O front-end é desenvolvido no **VS Code** e o back-end, no IntelliJ. Os dois ficam no mesmo repositório (monorepo).

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

Do front, o desafio exige **Angular 4 ou mais novo** e, como diferenciais, **design responsivo**, **login** e **perfis com permissão**. Login e perfis só entram se der tempo.

---

## 2. Como o front se encaixa na arquitetura

```
Navegador (Angular)  ──HTTP──►  backend :8080  ──►  PostgreSQL
```

- O back é um monólito modular, então o front fala com **um único endereço** (`/api/...`).
- Se o login for feito, o JWT é guardado e enviado em toda requisição (`Authorization: Bearer <token>`) por um **interceptor**. O `id` e o `perfil` (`ADMIN` ou `USUARIO`) vêm dentro do JWT.

---

## 3. Mobile-first

O design responsivo é a raiz do projeto: **cada tela nasce para o celular e depois é adaptada para o desktop.**

| Regra | Como aplicar |
|---|---|
| **CSS** | O estilo base é o do celular; telas maiores usam apenas `@media (min-width: ...)`, sem `max-width` |
| **Bootstrap** | Classes sem prefixo valem para o celular; `md` e `lg` adaptam para telas maiores |
| **Largura de referência** | Testar primeiro em **360 px**, sem rolagem horizontal da página |
| **Listas** | **Cards** no celular; **tabela** a partir de `md` |
| **Navegação** | Navbar colapsada no celular; expandida a partir de `lg` |
| **Formulários** | Uma coluna no celular; mais colunas só a partir de `md` |
| **Toque** | Botões e toggles com pelo menos 44 px de altura |

---

## 4. Stack

| Item | Escolha | Observação |
|---|---|---|
| Framework | **Angular** (versão estável atual) | Standalone components, sem NgModules; manter a versão fixa até a entrega |
| Linguagem | **TypeScript** | |
| Runtime | **Node.js LTS** (22 já instalado) | |
| CLI | **Angular CLI** (`ng`) | ⚠️ ainda não instalado |
| Estilo | **Bootstrap 5** + SCSS | Mobile-first |
| Formulários | **Reactive Forms** | Com validações |
| HTTP | **HttpClient** + interceptors funcionais | |
| Rotas | **Angular Router** | Guards só se o login for feito |
| Testes | Runner padrão do Angular CLI (Vitest ou Karma/Jasmine, conforme a versão) | Testes de services e components |
| Lint | **ESLint** (`ng add @angular-eslint/schematics`) | |
| Container | **Docker** (build Node → servir com **nginx**) | |

---

## 5. Estrutura de pastas

```
frontend/
└── src/app/
    ├── core/                      ← carregado uma vez e usado no app inteiro
    │   ├── interceptors/          ← interceptor de erros (mostra mensagem amigável)
    │   ├── layout/                ← navbar e estrutura da página
    │   └── auth/                  ← se der tempo: AuthService, guards, interceptor JWT
    ├── shared/                    ← componentes reutilizáveis (confirmação, alerta, loading)
    ├── models/                    ← interfaces: Usuario, Cartao, TipoCartao...
    ├── features/
    │   ├── usuarios/              ← lista e formulário de usuário + UsuarioService
    │   ├── cartoes/               ← lista, formulário e ativar/inativar + CartaoService
    │   └── login/                 ← se der tempo: tela de login
    ├── app.routes.ts              ← rotas
    └── app.config.ts              ← providers (HttpClient, interceptors, router)
    environments/                  ← apiUrl de dev e de produção
```

---

## 6. Telas

| Tela | Rota | O que faz |
|---|---|---|
| Usuários | `/usuarios` | Lista com todos, com a quantidade de cartões e ações de editar e remover |
| Novo usuário | `/usuarios/novo` | Formulário (nome, email e senha obrigatórios) |
| Editar usuário | `/usuarios/:id` | Mesmo formulário; senha vazia mantém a atual |
| Cartões | `/cartoes` | Lista com todos, filtro por usuário, toggle ativo/inativo e remover |
| Novo cartão | `/cartoes/novo` | Formulário (usuário, número, nome, tipo) |

- **Rotas:** declarar `/usuarios/novo` antes de `/usuarios/:id`, senão `novo` é lido como um id.
- **Nome do dono do cartão:** a API de cartões devolve o `usuarioId`, e o front busca o nome em `GET /api/usuarios`.
- **Feedback:** loading, mensagem de sucesso ou erro, estado vazio e confirmação antes de remover.

### Se o login for feito

| Tela | Perfil | Comportamento |
|---|---|---|
| Login (`/login`) | Público | Email e senha, guarda o JWT |
| Usuários e formulários | ADMIN | As rotas são bloqueadas pelo guard para o USUARIO |
| Cartões | ADMIN, USUARIO | O USUARIO vê só os próprios cartões, sem filtro e sem ações |

O formulário de usuário ganha o campo **perfil**.

---

## 7. API consumida

| Método | Rota | Uso |
|---|---|---|
| GET / POST | `/api/usuarios` | listar (com os cartões) / incluir |
| GET / PUT / DELETE | `/api/usuarios/{id}` | buscar / alterar / remover |
| GET | `/api/cartoes?usuarioId=` | listar (filtro opcional) |
| POST | `/api/cartoes` | adicionar cartão a um usuário |
| DELETE | `/api/cartoes/{id}` | remover cartão |
| PATCH | `/api/cartoes/{id}/status` | ativar/inativar `{ "status": true }` |
| POST | `/api/auth/login` | login (se der tempo) |

Os detalhes do back-end estão em `docs/backend/01-PROJETO-BACKEND.md`.

---

## 8. Ambiente

| Ambiente | URL do front                                               | apiUrl |
|---|------------------------------------------------------------|---|
| Dev | http://localhost:4200 ou, na rede, http://<IP DA MAQUINA > | `/api` (o proxy do Angular repassa para `localhost:8080`) |
| Docker | http://host (porta 80, nginx)                              | `/api` (o nginx repassa para o backend) |

### Acompanhar no tablet (dev)

- `npm start` roda `ng serve --host 0.0.0.0 --proxy-config proxy.conf.json`, que aceita conexões da rede local.
- O `proxy.conf.json` repassa `/api` para `http://localhost:8080`. Assim, o tablet só fala com o Angular, e o back continua exposto apenas em `127.0.0.1`.
- O `apiUrl` é sempre relativo (`/api`), em dev e no Docker, então o mesmo código funciona no PC, no tablet e no nginx.
- O IP do PC vem do roteador (DHCP) e pode mudar: conferir com `hostname -I`.
- Se o `ufw` bloquear, liberar só a rede de casa: `sudo ufw allow from <REDE LOCAL> to any port 4200 proto tcp`.
- Usar apenas na rede de casa, nunca em Wi-Fi público.
