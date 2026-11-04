import { ROTULO_TIPO_CARTAO, TIPOS_CARTAO } from './tipo-cartao';

describe('TipoCartao', () => {
  it('lista os 3 tipos aceitos pela API, na ordem do select', () => {
    expect(TIPOS_CARTAO).toEqual(['COMUM', 'ESTUDANTE', 'TRABALHADOR']);
  });

  it('tem um rótulo para cada tipo', () => {
    for (const tipo of TIPOS_CARTAO) {
      expect(ROTULO_TIPO_CARTAO[tipo]).toBeTruthy();
    }
  });
});
