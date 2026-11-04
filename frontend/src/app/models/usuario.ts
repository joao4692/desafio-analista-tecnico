import { CartaoResumo } from './cartao';

// Resposta de GET /api/usuarios. Não tem senha: o back nunca a devolve.
export interface Usuario {
  id: number;
  nome: string;
  email: string;
  // Data em texto ISO (ex.: "2026-11-02T16:29:41"); a tela formata com o pipe date.
  criadoEm: string;
  cartoes: CartaoResumo[];
}

// Corpo de POST /api/usuarios: a senha é obrigatória.
export interface UsuarioCreateRequest {
  nome: string;
  email: string;
  senha: string;
}

// Corpo de PUT /api/usuarios/{id}: senha vazia mantém a atual.
export interface UsuarioUpdateRequest {
  nome: string;
  email: string;
  senha: string;
}
