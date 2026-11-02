package br.com.desafio.shared.exception;

// Lançada quando a operação bate de frente com um dado que já existe (e-mail ou número de cartão repetido).
// O GlobalExceptionHandler transforma em 409 Conflict.
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
