# Resumo da sessão

> **Para a IA:** leia este arquivo e `docs/REGRAS-IA.md` ao retomar. Confira com
> `git status` e `git log`. Abra os outros documentos só quando precisar de um detalhe.

**Última atualização:** 04/11/2026 (dia da entrega)

## Onde paramos
- **Back obrigatório (Etapas 0 a 9) e front obrigatório (Etapas 0 a 7):** completos e na `develop` (PRs até o #17)
- **PR #18 (README final) aberto**, branch `docs/readme-final`; a máquina local está nessa branch: voltar para a `develop`
- **A `main` está desatualizada:** só tem o back e o esqueleto do front (PR #10). Falta o PR `develop` → `main` e a tag `v1.0.0`
- **Próximo:** login e perfis (Etapa 10 do back e 8 do front). Recomendado fazer a release na `main` antes

## O que está pronto no front (Angular 22, VS Code)
- Telas: `/usuarios` (lista), `/usuarios/novo` e `/usuarios/:id` (form), `/cartoes` (lista com filtro e switch ativo/inativo) e `/cartoes/novo`
- Mobile-first: cards e menu colapsado no celular; tabelas e formulários em 2 colunas a partir de `md`
- `erroInterceptor` mostra a `mensagem` do back no `AlertaService`; os services ficam em `features/*/`
- 61 testes (Vitest) e CI `frontend` (lint, testes e build); a `main` exige os checks `build` e `frontend`
- Docker: `docker compose up --build` sobe `postgres`, `backend` e `frontend` (nginx na porta 80)
- Back: 97 testes, cobertura de 99% das instruções e 100% dos desvios

## Decisões que afetam o login
- O front sempre chama `/api` (relativo). No dev, `proxy.conf.mjs` repassa para `localhost:8080`; no Docker, o nginx repassa para `backend:8080`
- **Os dois proxies removem o cabeçalho `Origin`**: sem isso, o CORS do back dava 403 em POST/PUT/PATCH/DELETE vindos do tablet e do Docker. O `Authorization` passa normalmente pelos dois
- Ao ligar o Spring Security, o front para de funcionar até a Etapa 8 do front (interceptor JWT, tela de login e guards). Manter o login em branch própria e só juntar na `develop` com o front pronto
- O front trata 401/403 pelo `erroInterceptor`; na Etapa 8, o 401 deve levar para `/login`
- `Usuario` (model do front) e o formulário de usuário vão precisar do campo `perfil`

## Como trabalhamos
- Branch `feature/...` → commits separados → PR para a `develop` → CI verde → merge → apagar a branch → `git pull` + `git fetch --prune`
- **Conferir sempre o `base: develop` no PR**: o botão amarelo do GitHub abre para a `main` (já aconteceu no PR #10)
- Front no tablet: `npm start` em `http://192.168.0.29:4200`, ou o Docker em `http://192.168.0.29`; reiniciar o `npm start` depois de trocar de branch

## Pendências
- Decidir se `docs/` e o `REGRAS-IA.md` vão para o repositório (hoje estão fora)
- `frontend/README.md` ainda é o padrão do Angular CLI, em inglês
- Migrações: nunca editar uma já aplicada
