package br.com.desafio.cartao.dto;

import br.com.desafio.cartao.domain.TipoCartao;

// Saída da API de cartão. O tipo sai como texto no JSON (ex.: "ESTUDANTE").
public record CartaoResponse(
        Long id,
        Long numeroCartao,
        String nome,
        boolean status,
        TipoCartao tipoCartao,
        Long usuarioId
) {
}
