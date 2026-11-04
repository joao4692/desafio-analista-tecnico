package br.com.desafio.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Entrada do POST /api/usuarios.
// Record: imutável, com construtor e "getters" gerados pelo Java. É o que substitui o Lombok aqui.
// As anotações são checadas pelo @Valid no controller (5.8): se algo falhar, a resposta é 400
// e a requisição nem chega ao service.
public record UsuarioCreateRequest(

        @NotBlank
        @Size(max = 120)
        String nome,

        // Os tamanhos repetem os da V1: o usuário recebe um 400 claro, e não um erro do banco.
        @NotBlank
        @Email
        @Size(max = 160)
        String email,

        // Obrigatória no cadastro. O máximo é 72 porque o BCrypt ignora o que passa de 72 bytes.
        @NotBlank
        @Size(min = 6, max = 72)
        String senha
) {
}
