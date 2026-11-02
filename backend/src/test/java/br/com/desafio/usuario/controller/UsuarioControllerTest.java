package br.com.desafio.usuario.controller;

import br.com.desafio.cartao.api.CartaoResumo;
import br.com.desafio.shared.exception.ConflitoException;
import br.com.desafio.shared.exception.RecursoNaoEncontradoException;
import br.com.desafio.usuario.dto.UsuarioResponse;
import br.com.desafio.usuario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest sobe só a camada web: este controller, o GlobalExceptionHandler (ele é carregado sozinho,
// por ser um @RestControllerAdvice), a validação e a conversão para JSON. Não sobe banco nem service.
// O service é um mock (@MockitoBean): aqui só importa se o HTTP está certo (rota, status, JSON, validação).
@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    private final UsuarioResponse ana =
            new UsuarioResponse(1L, "Ana", "ana@email.com", LocalDateTime.of(2026, 11, 2, 10, 0), List.of());

    @Test
    void listarDeveRetornar200SemExporASenha() throws Exception {
        when(service.listar()).thenReturn(List.of(ana));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ana"))
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    @Test
    void buscarPorIdDeveRetornar200() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(ana);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@email.com"));
    }

    @Test
    void buscarPorIdDeveIncluirOsCartoesNoJson() throws Exception {
        var comCartao = new UsuarioResponse(1L, "Ana", "ana@email.com", LocalDateTime.of(2026, 11, 2, 10, 0),
                List.of(new CartaoResumo(10L, 1001L, "Cartão da Ana", true, "ESTUDANTE")));
        when(service.buscarPorId(1L)).thenReturn(comCartao);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartoes[0].numeroCartao").value(1001))
                .andExpect(jsonPath("$.cartoes[0].tipoCartao").value("ESTUDANTE"));
    }

    @Test
    void buscarPorIdInexistenteDeveRetornar404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Usuário 99 não encontrado"));

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário 99 não encontrado"));
    }

    @Test
    void idNaoNumericoDeveRetornar400() throws Exception {
        mockMvc.perform(get("/api/usuarios/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarDeveRetornar201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(ana);

        // Text block (""" ... """): o JSON fica legível, sem escapar as aspas.
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Ana", "email": "ana@email.com", "senha": "senha123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/usuarios/1")))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void criarComDadosInvalidosDeveRetornar400SemChamarOService() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "email": "isso-nao-e-email", "senha": "123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("nome")))
                .andExpect(jsonPath("$.mensagem", containsString("email")))
                .andExpect(jsonPath("$.mensagem", containsString("senha")));

        verify(service, never()).criar(any());
    }

    @Test
    void mensagensDeValidacaoDevemSairEmPortugues() throws Exception {
        // Mesmo com o navegador pedindo inglês (Accept-Language: en), o idioma fixo da API vence.
        mockMvc.perform(post("/api/usuarios")
                        .header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "email": "ana@email.com", "senha": "senha123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("nome: não deve estar em branco"));
    }


    @Test
    void criarComEmailDuplicadoDeveRetornar409() throws Exception {
        when(service.criar(any())).thenThrow(new ConflitoException("E-mail já cadastrado: ana@email.com"));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Ana", "email": "ana@email.com", "senha": "senha123"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void atualizarSemSenhaDeveRetornar200() throws Exception {
        when(service.atualizar(eq(1L), any())).thenReturn(ana);

        // Sem o campo "senha" no JSON: ele chega como null e a senha atual é mantida (regra do service).
        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Ana", "email": "ana@email.com"}
                                """))
                .andExpect(status().isOk());

        verify(service).atualizar(eq(1L), any());
    }

    @Test
    void atualizarComSenhaCurtaDeveRetornar400() throws Exception {
        // Prova o @Pattern do UsuarioUpdateRequest: "" é aceito, mas "123" não.
        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Ana", "email": "ana@email.com", "senha": "123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("senha")));
    }

    @Test
    void removerDeveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(service).remover(1L);
    }
}
