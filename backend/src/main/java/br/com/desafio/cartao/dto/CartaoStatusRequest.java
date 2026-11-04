package br.com.desafio.cartao.dto;

import jakarta.validation.constraints.NotNull;

// Entrada do PATCH /api/cartoes/{id}/status. Ex.: { "status": false } inativa o cartão.
// Boolean (com B maiúsculo) e não boolean: assim um JSON sem o campo chega como null
// e o @NotNull devolve 400. Com boolean, ele viraria "false" e inativaria o cartão sem querer.
public record CartaoStatusRequest(
        @NotNull
        Boolean status
) {
}
