package br.com.desafio.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Entrada do PUT /api/usuarios/{id}. Nome e e-mail são obrigatórios, como no cadastro.
public record UsuarioUpdateRequest(

        @NotBlank
        @Size(max = 120)
        String nome,

        @NotBlank
        @Email
        @Size(max = 160)
        String email,

        // Opcional: null ou vazio mantém a senha atual (regra do 5.7).
        // Um @Size(min = 6) rejeitaria o vazio, por isso a regra é um @Pattern:
        // aceita "" OU de 6 a 72 caracteres. O null passa direto.
        @Pattern(regexp = "^$|^.{6,72}$", message = "deve ter entre 6 e 72 caracteres")
        String senha
) {
}
