import { TipoCartao } from './tipo-cartao';

// Resposta de GET /api/cartoes (CartaoResponse no back).
export interface Cartao {
  id: number;
  numeroCartao: number;
  nome: string;
  // true = ativo.
  status: boolean;
  tipoCartao: TipoCartao;
  // Só o id: o nome do dono vem de GET /api/usuarios (os módulos são separados no back).
  usuarioId: number;
}

// Cartão dentro do usuário (CartaoResumo no back). Não tem usuarioId,
// porque já vem agrupado no usuário.
export type CartaoResumo = Omit<Cartao, 'usuarioId'>;

// Corpo de POST /api/cartoes. O id e o status são definidos pelo back.
export type CartaoCreateRequest = Omit<Cartao, 'id' | 'status'>;

// Corpo de PATCH /api/cartoes/{id}/status.
export interface CartaoStatusRequest {
  status: boolean;
}
