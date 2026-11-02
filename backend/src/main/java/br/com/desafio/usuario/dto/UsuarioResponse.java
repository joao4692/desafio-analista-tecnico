package br.com.desafio.usuario.dto;

import br.com.desafio.cartao.api.CartaoResumo;

import java.time.LocalDateTime;
import java.util.List;

// Saída da API. Não tem campo de senha: nem o hash sai do back-end.
// "cartoes" vem do módulo de cartão pelo contrato CartaoConsulta (7.1):
// o módulo de usuário só conhece o CartaoResumo, nunca a entidade Cartao.
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        LocalDateTime criadoEm,
        List<CartaoResumo> cartoes
) {
}
