package br.com.desafio.shared.exception;

import java.time.LocalDateTime;

// Formato único de erro da API: seja qual for o erro, o front recebe sempre estes 5 campos.
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,      // nome do status HTTP, ex.: "Not Found"
        String mensagem,  // explicação para quem chamou a API
        String caminho    // rota que deu erro, ex.: "/api/usuarios/99"
) {
}
