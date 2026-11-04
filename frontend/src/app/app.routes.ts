import { Routes } from '@angular/router';

const carregarFormUsuario = () =>
  import('./features/usuarios/usuario-form/usuario-form').then((m) => m.UsuarioForm);

// Cada tela é carregada só quando é aberta.
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'usuarios' },
  {
    path: 'usuarios',
    title: 'Usuários | Cartões de Ônibus',
    loadComponent: () =>
      import('./features/usuarios/usuario-lista/usuario-lista').then((m) => m.UsuarioLista),
  },
  // "novo" vem antes de ":id"; senão seria lido como um id.
  {
    path: 'usuarios/novo',
    title: 'Novo usuário | Cartões de Ônibus',
    loadComponent: carregarFormUsuario,
  },
  {
    path: 'usuarios/:id',
    title: 'Editar usuário | Cartões de Ônibus',
    loadComponent: carregarFormUsuario,
  },
  {
    path: 'cartoes',
    title: 'Cartões | Cartões de Ônibus',
    loadComponent: () =>
      import('./features/cartoes/cartao-lista/cartao-lista').then((m) => m.CartaoLista),
  },
  { path: '**', redirectTo: 'usuarios' },
];
