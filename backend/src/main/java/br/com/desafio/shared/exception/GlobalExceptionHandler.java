package br.com.desafio.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

// @RestControllerAdvice: "escuta" as exceções que escapam de QUALQUER controller e vira o ponto único
// de tratamento de erro. Os controllers e services não montam resposta de erro; só lançam a exceção.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                            HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> tratarConflito(ConflitoException ex, HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // Plano B do service: se duas requisições cadastram o mesmo e-mail ao mesmo tempo,
    // as duas passam na checagem do service, mas o banco (uk_usuario_email) barra uma delas.
    // A mensagem do banco NÃO é repassada, porque expõe nomes de tabela e de constraint.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarIntegridade(DataIntegrityViolationException ex,
                                                          HttpServletRequest request) {
        log.warn("Violação de integridade no banco em {}", request.getRequestURI(), ex);
        return montar(HttpStatus.CONFLICT,
                "A operação viola uma regra de unicidade ou de relacionamento dos dados", request);
    }

    // Erros do @Valid: junta todos os campos inválidos numa mensagem só.
    // Ex.: "email: deve ser um endereço de e-mail bem formado; nome: não deve estar em branco"
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .sorted() // ordem fixa: facilita ler e testar
                .collect(Collectors.joining("; "));
        return montar(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException ex,
                                                            HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou mal formatado", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                           HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + ex.getName() + "'", request);
    }

    // Rede de segurança para o que não foi previsto acima.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        // As exceções do próprio Spring MVC (rota inexistente = 404, método errado = 405...)
        // já carregam o status certo. Sem este "if", todas virariam 500.
        if (ex instanceof ErrorResponse erroDoSpring) {
            HttpStatus status = HttpStatus.valueOf(erroDoSpring.getStatusCode().value());
            return montar(status, erroDoSpring.getBody().getDetail(), request);
        }
        // Bug de verdade: o detalhe vai para o log, para investigar, e NUNCA para o cliente.
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.", request);
    }

    private ResponseEntity<ErroResponse> montar(HttpStatus status, String mensagem, HttpServletRequest request) {
        ErroResponse corpo = new ErroResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(corpo);
    }
}
