package br.com.desafio.cartao.dto;

import br.com.desafio.cartao.domain.TipoCartao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Entrada do POST /api/cartoes. Validada pelo @Valid no controller (6.7).
public record CartaoCreateRequest(

        // @Positive repete o CHECK ck_cartao_numero_positivo da V2: o erro vira um 400 claro, e não um erro do banco.
        @NotNull
        @Positive
        Long numeroCartao,

        @NotBlank
        @Size(max = 120)
        String nome,

        // Um valor fora do enum (ex.: "VIP") nem chega aqui: o JSON falha ao ser lido
        // e o GlobalExceptionHandler responde 400 ("Corpo da requisição inválido").
        @NotNull
        TipoCartao tipoCartao,

        // Se o usuário existe ou não é verificado no service (6.6), via contrato UsuarioConsulta.
        @NotNull
        @Positive
        Long usuarioId
) {
}
