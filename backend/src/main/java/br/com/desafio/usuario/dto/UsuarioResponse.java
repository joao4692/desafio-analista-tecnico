package br.com.desafio.usuario.dto;

import java.time.LocalDateTime;

// Saída da API. Não tem campo de senha: nem o hash sai do back-end.
// No Passo 7.2 ele ganha a lista de cartões do usuário.
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        LocalDateTime criadoEm
) {
}
