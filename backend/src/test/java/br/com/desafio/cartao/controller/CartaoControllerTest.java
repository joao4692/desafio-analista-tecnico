package br.com.desafio.cartao.controller;

import br.com.desafio.cartao.domain.TipoCartao;
import br.com.desafio.cartao.dto.CartaoResponse;
import br.com.desafio.cartao.service.CartaoService;
import br.com.desafio.shared.exception.ConflitoException;
import br.com.desafio.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Mesmo modelo do UsuarioControllerTest: só a camada web, com o service como mock.
@WebMvcTest(CartaoController.class)
class CartaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartaoService service;

    private final CartaoResponse cartao =
            new CartaoResponse(10L, 1001L, "Cartão da Ana", true, TipoCartao.ESTUDANTE, 1L);

    @Test
    void listarSemFiltroDevePassarNullParaOService() throws Exception {
        when(service.listar(null)).thenReturn(List.of(cartao));

        mockMvc.perform(get("/api/cartoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCartao").value(1001))
                .andExpect(jsonPath("$[0].tipoCartao").value("ESTUDANTE")); // o enum sai como texto
    }

    @Test
    void listarComFiltroDevePassarOUsuarioIdParaOService() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(cartao));

        mockMvc.perform(get("/api/cartoes").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(1));

        verify(service).listar(1L);
    }

    @Test
    void adicionarDeveRetornar201ComLocation() throws Exception {
        when(service.adicionar(any())).thenReturn(cartao);

        mockMvc.perform(post("/api/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCartao": 1001, "nome": "Cartão da Ana", "tipoCartao": "ESTUDANTE", "usuarioId": 1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/cartoes/10")))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void adicionarComDadosInvalidosDeveRetornar400SemChamarOService() throws Exception {
        // Número negativo, nome vazio, e tipoCartao e usuarioId ausentes: os 4 campos com erro.
        mockMvc.perform(post("/api/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCartao": -5, "nome": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("numeroCartao")))
                .andExpect(jsonPath("$.mensagem", containsString("nome")))
                .andExpect(jsonPath("$.mensagem", containsString("tipoCartao")))
                .andExpect(jsonPath("$.mensagem", containsString("usuarioId")));

        verify(service, never()).adicionar(any());
    }

    @Test
    void adicionarComTipoInexistenteDeveRetornar400() throws Exception {
        // "VIP" não existe no enum: o JSON nem é lido, e o handler responde 400.
        mockMvc.perform(post("/api/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCartao": 1001, "nome": "Cartão", "tipoCartao": "VIP", "usuarioId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Corpo da requisição inválido ou mal formatado"));
    }

    @Test
    void adicionarParaUsuarioInexistenteDeveRetornar404() throws Exception {
        when(service.adicionar(any())).thenThrow(new RecursoNaoEncontradoException("Usuário 99 não encontrado"));

        mockMvc.perform(post("/api/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCartao": 1001, "nome": "Cartão", "tipoCartao": "COMUM", "usuarioId": 99}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário 99 não encontrado"));
    }

    @Test
    void adicionarComNumeroRepetidoDeveRetornar409() throws Exception {
        when(service.adicionar(any())).thenThrow(new ConflitoException("Número de cartão já cadastrado: 1001"));

        mockMvc.perform(post("/api/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCartao": 1001, "nome": "Cartão", "tipoCartao": "COMUM", "usuarioId": 1}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void alterarStatusDeveRetornar200ComOCartaoAtualizado() throws Exception {
        var inativo = new CartaoResponse(10L, 1001L, "Cartão da Ana", false, TipoCartao.ESTUDANTE, 1L);
        when(service.alterarStatus(eq(10L), any())).thenReturn(inativo);

        mockMvc.perform(patch("/api/cartoes/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void alterarStatusSemOCampoDeveRetornar400() throws Exception {
        // Prova o Boolean do CartaoStatusRequest: o campo ausente vira null, e o @NotNull barra.
        // Com boolean, viraria "false" e inativaria o cartão sem querer.
        mockMvc.perform(patch("/api/cartoes/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("status")));

        verify(service, never()).alterarStatus(any(), any());
    }

    @Test
    void removerDeveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/cartoes/10"))
                .andExpect(status().isNoContent());

        verify(service).remover(10L);
    }
}
