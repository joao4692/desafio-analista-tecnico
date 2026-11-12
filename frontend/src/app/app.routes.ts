import { Routes } from '@angular/router';

const carregarFormUsuario = () =>
  import('./features/usuarios/usuario-form/usuario-form').then((m) => m.UsuarioForm);

// Cada tela é carregada só quando é aberta.
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'usuarios' },
  {
    path: 'usuarios',
    title: 'Usuários | ValeIR',
    loadComponent: () =>
      import('./features/usuarios/usuario-lista/usuario-lista').then((m) => m.UsuarioLista),
  },
  // "novo" vem antes de ":id"; senão seria lido como um id.
  {
    path: 'usuarios/novo',
    title: 'Novo usuário | ValeIR',
    loadComponent: carregarFormUsuario,
  },
  {
    path: 'usuarios/:id',
    title: 'Editar usuário | ValeIR',
    loadComponent: carregarFormUsuario,
  },
  {
    path: 'cartoes',
    title: 'Cartões | ValeIR',
    loadComponent: () =>
      import('./features/cartoes/cartao-lista/cartao-lista').then((m) => m.CartaoLista),
  },
  {
    path: 'cartoes/novo',
    title: 'Novo cartão | ValeIR',
    loadComponent: () =>
      import('./features/cartoes/cartao-form/cartao-form').then((m) => m.CartaoForm),
  },
  { path: '**', redirectTo: 'usuarios' },
];
