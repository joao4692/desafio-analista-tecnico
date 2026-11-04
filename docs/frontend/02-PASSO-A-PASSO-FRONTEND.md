# Passo a passo: Front-end

> **Para a IA:** este é o roteiro e o status atual. Continue do primeiro passo `[a fazer]`.
> Siga `docs/REGRAS-IA.md`: **eu crio os arquivos**, você me diz o que criar e me manda o código comentado.
> A arquitetura e as decisões estão em `docs/frontend/01-PROJETO-FRONTEND.md`.

**Legenda:** `[concluído]` · `[a fazer]` · `[em andamento]` · ✅ = etapa ou passo concluído

> **Pré-requisito:** concluir a **Etapa 9 do back-end**, quando a API, o CORS e o Docker do back já estão prontos.
> **Mobile-first:** toda tela das Etapas 3 a 5 é feita e testada primeiro em 360 px. O desktop vem na Etapa 6.

---

## ✅ Etapa 0: Ferramentas
Conferido em 03/11/2026 nesta máquina.

| Ferramenta | Necessária | Situação |
|---|---|---|
| Node.js | LTS (22 ou mais novo) | ✅ v22.23.1 (via `fnm`) |
| npm | 10 ou mais novo | ✅ 10.9.8 |
| Angular CLI | mais recente | ✅ 22.2.5 |
| Git | 2.x | ✅ 2.43.0 |
| Docker | recente | ✅ 29.9.0 |
| VS Code | recente | ✅ |

- ✅ Passo 0.1: Confirmar Node e npm `[concluído]`
- ✅ Passo 0.2: Instalar o Angular CLI com `npm install -g @angular/cli` e conferir com `ng version` `[concluído]`
- ✅ Passo 0.3: Extensões do VS Code: **Angular Language Service**, **ESLint** e **Prettier** `[concluído]`

---

## ✅ Etapa 1: Criar o projeto
Branch: `feature/front-esqueleto`
- ✅ Passo 1.1: Na raiz, rodar `ng new frontend` (SCSS, sem SSR, standalone) `[concluído]`
- ✅ Passo 1.2: Conferir que o `ng new` **não criou um `.git` dentro de `frontend/`**. Se criou, remover, porque o repositório é um só (monorepo) `[concluído]`
- ✅ Passo 1.3: Instalar o Bootstrap 5 e configurá-lo no `angular.json` / `styles.scss` `[concluído]`
- ✅ Passo 1.4: Adicionar o ESLint (`ng add angular-eslint`) `[concluído]`
- ✅ Passo 1.5: Criar as pastas `core/`, `shared/`, `models/` e `features/`, os `environments` (`apiUrl: '/api'`) e o `proxy.conf.json`, e ajustar o `npm start` para `--host 0.0.0.0` com o proxy `[concluído]`
- ✅ Passo 1.6: Rodar `npm start` e `ng test` com sucesso, e abrir no tablet em `http://<IP-da-máquina>:4200` `[concluído]`

---

## ✅ Etapa 2: CI do front
Branch: `feature/front-ci`
- ✅ Passo 2.1: Criar `.github/workflows/frontend-ci.yml` (Node LTS, `npm ci`, lint, testes headless e build) `[concluído]`
- ✅ Passo 2.2: PR com o pipeline verde e badge no README `[concluído]`

---

## ✅ Etapa 3: Base da aplicação (celular)
Branch: `feature/front-base`
- ✅ Passo 3.1: Models (`Usuario`, `Cartao`, `TipoCartao`) `[concluído]`
- ✅ Passo 3.2: Layout (navbar colapsada) e rotas iniciais `[concluído]`
- ✅ Passo 3.3: Interceptor de erros e componente de alerta, com teste `[concluído]`

---

## ✅ Etapa 4: Usuários (celular)
Branch: `feature/front-usuarios`
- ✅ Passo 4.1: `UsuarioService` (HttpClient) e **teste** `[concluído]`
- ✅ Passo 4.2: Lista em cards, com a quantidade de cartões e remoção com confirmação, e teste `[concluído]`
- ✅ Passo 4.3: Formulário (Reactive Forms com validações; senha obrigatória só ao incluir) para incluir e editar, e teste `[concluído]`

---

## ✅ Etapa 5: Cartões (celular)
Branch: `feature/front-cartoes`
- ✅ Passo 5.1: `CartaoService` e teste `[concluído]`
- ✅ Passo 5.2: Lista em cards, com nome do dono, filtro por usuário, toggle ativo/inativo e remoção, e teste `[concluído]`
- ✅ Passo 5.3: Formulário de novo cartão (select de usuário e de tipo) e teste `[concluído]`

---

## ✅ Etapa 6: Desktop e acabamento
Branch: `feature/front-desktop`
- ✅ Passo 6.1: Listas viram tabelas a partir de `md`, e teste `[concluído]`
- ✅ Passo 6.2: Navbar expandida a partir de `lg` e formulários em mais colunas `[concluído]`
- ✅ Passo 6.3: Revisão em celular, tablet e desktop `[concluído]`
- ✅ Passo 6.4: Loading, estados vazios ("nenhum cartão") e mensagens `[concluído]`

---

## ✅ Etapa 7: Docker
Branch: `feature/front-docker`
- ✅ Passo 7.1: `Dockerfile` multi-stage (build Node → nginx) `[concluído]`
- ✅ Passo 7.2: `nginx.conf` (rotas do Angular e proxy de `/api` para o backend) `[concluído]`
- ✅ Passo 7.3: Adicionar ao `docker-compose.yml` e testar tudo com `docker compose up --build` `[concluído]`

> **Voltar ao back-end:** as próximas etapas são as opcionais do back (a partir da Etapa 10) e a Entrega (Etapa 13).

---

## Etapa 8: Login e perfis (se der tempo)
Branch: `feature/front-auth`

Só começa depois da Etapa 10 do back-end.

- Passo 8.1: Models `LoginRequest` e `LoginResponse`, e `AuthService` (login, guardar o token, ler `id` e `perfil`, logout), com teste `[a fazer]`
- Passo 8.2: Tela de login, mobile-first, e teste `[a fazer]`
- Passo 8.3: Interceptor JWT (adiciona o `Authorization`) e teste `[a fazer]`
- Passo 8.4: `authGuard` e `roleGuard` nas rotas, e teste `[a fazer]`
- Passo 8.5: Esconder ações de ADMIN e o filtro por usuário para o USUARIO, e teste `[a fazer]`
- Passo 8.6: Campo `perfil` no formulário de usuário, e teste `[a fazer]`
