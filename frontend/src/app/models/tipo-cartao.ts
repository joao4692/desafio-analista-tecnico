// Mesmos valores do enum TipoCartao do back. Usar um union type faz o TypeScript
// recusar qualquer outro texto, como 'estudante' em minúsculas.
export type TipoCartao = 'COMUM' | 'ESTUDANTE' | 'TRABALHADOR';

// Lista usada para montar o select do formulário, na ordem em que aparece.
export const TIPOS_CARTAO: readonly TipoCartao[] = ['COMUM', 'ESTUDANTE', 'TRABALHADOR'];

// Texto exibido na tela. O Record obriga a ter um rótulo para cada tipo:
// se o back ganhar um tipo novo, o build quebra até ele ser incluído aqui.
export const ROTULO_TIPO_CARTAO: Record<TipoCartao, string> = {
  COMUM: 'Comum',
  ESTUDANTE: 'Estudante',
  TRABALHADOR: 'Trabalhador',
};
