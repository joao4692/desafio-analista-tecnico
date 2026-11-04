import { Routes } from '@angular/router';

// Cada tela é carregada só quando é aberta.
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'usuarios' },
  {
    path: 'usuarios',
    title: 'Usuários | Cartões de Ônibus',
    loadComponent: () =>
      import('./features/usuarios/usuario-lista/usuario-lista').then((m) => m.UsuarioLista),
  },
  {
    path: 'cartoes',
    title: 'Cartões | Cartões de Ônibus',
    loadComponent: () =>
      import('./features/cartoes/cartao-lista/cartao-lista').then((m) => m.CartaoLista),
  },
  { path: '**', redirectTo: 'usuarios' },
];
