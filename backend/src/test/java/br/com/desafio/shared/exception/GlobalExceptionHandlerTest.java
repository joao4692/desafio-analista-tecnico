package br.com.desafio.shared.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testa o handler sem subir o Spring inteiro nem o banco.
// O MockMvc "standalone" monta só um controller falso (que lança cada tipo de erro) e o nosso handler.
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControllerFalso())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void recursoNaoEncontradoDeveVirar404NoFormatoPadrao() throws Exception {
        // Este teste confere os 5 campos; os outros conferem só o que muda.
        mockMvc.perform(get("/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value("Usuário 99 não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/teste/nao-encontrado"));
    }

    @Test
    void conflitoDeveVirar409() throws Exception {
        mockMvc.perform(get("/teste/conflito"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("E-mail já cadastrado"));
    }

    @Test
    void violacaoNoBancoDeveVirar409SemExporDetalhesDoBanco() throws Exception {
        mockMvc.perform(get("/teste/integridade"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", not(containsString("uk_usuario_email"))));
    }

    @Test
    void campoInvalidoDeveVirar400ComONomeDoCampo() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("nome")));
    }

    @Test
    void jsonMalFormadoDeveVirar400() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ isso não é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Corpo da requisição inválido ou mal formatado"));
    }

    @Test
    void metodoNaoSuportadoDeveManterO405() throws Exception {
        // Prova o "if" da rede de segurança: um erro do próprio Spring não pode virar 500.
        mockMvc.perform(delete("/teste/nao-encontrado"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void erroInesperadoDeveVirar500SemVazarDetalhes() throws Exception {
        mockMvc.perform(get("/teste/erro-inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem", not(containsString("detalhe interno"))));
    }

    // Controller que só existe neste teste: cada rota lança um tipo de erro.
    @RestController
    static class ControllerFalso {

        @GetMapping("/teste/nao-encontrado")
        void naoEncontrado() {
            throw new RecursoNaoEncontradoException("Usuário 99 não encontrado");
        }

        @GetMapping("/teste/conflito")
        void conflito() {
            throw new ConflitoException("E-mail já cadastrado");
        }

        @GetMapping("/teste/integridade")
        void integridade() {
            throw new DataIntegrityViolationException("duplicate key uk_usuario_email");
        }

        @GetMapping("/teste/erro-inesperado")
        void erroInesperado() {
            throw new IllegalStateException("detalhe interno");
        }

        @PostMapping("/teste/validacao")
        void validacao(@Valid @RequestBody DadosFalsos dados) {
        }
    }

    record DadosFalsos(@NotBlank String nome) {
    }
}
