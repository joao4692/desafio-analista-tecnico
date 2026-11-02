package br.com.desafio.shared.exception;

// Lançada quando um id não existe no banco. O GlobalExceptionHandler transforma em 404.
// RuntimeException (unchecked): o @Transactional desfaz a transação automaticamente
// e não obriga a declarar "throws" em todo método.
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
