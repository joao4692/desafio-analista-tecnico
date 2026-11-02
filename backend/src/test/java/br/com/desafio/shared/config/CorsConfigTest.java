package br.com.desafio.shared.config;

import br.com.desafio.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Simula o que o NAVEGADOR faz: manda o header "Origin" e, antes de um POST, um OPTIONS (preflight).
// Mesmas anotações dos outros testes de integração: o contexto e o banco são reaproveitados.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CorsConfigTest {

    private static final String FRONT = "http://localhost:4200";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightDoFrontDeveSerPermitido() throws Exception {
        // O navegador pergunta: "o localhost:4200 pode fazer um POST aqui?"
        mockMvc.perform(options("/api/usuarios")
                        .header(HttpHeaders.ORIGIN, FRONT)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONT))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")));
    }

    @Test
    void respostaParaOFrontDeveLiberarAOrigemEExporOLocation() throws Exception {
        mockMvc.perform(get("/api/usuarios").header(HttpHeaders.ORIGIN, FRONT))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONT))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, containsString("Location")));
    }

    @Test
    void origemDesconhecidaDeveSerRecusada() throws Exception {
        // Outro site tentando usar a API pelo navegador de alguém: o Spring responde 403 ao preflight.
        mockMvc.perform(options("/api/usuarios")
                        .header(HttpHeaders.ORIGIN, "http://site-desconhecido.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
