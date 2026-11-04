package br.com.desafio.shared.config;

import br.com.desafio.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Garante que a documentação existe e cobre as rotas: se um controller sumir do Swagger, este teste falha.
// Mesmas anotações dos outros testes de integração: o Spring reaproveita o contexto e o banco, sem custo extra.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OpenApiDocumentacaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveDocumentarTodasAsRotasDaApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API de Usuários e Cartões de Ônibus"))
                .andExpect(jsonPath("$.paths['/api/usuarios']").exists())
                .andExpect(jsonPath("$.paths['/api/usuarios/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/cartoes']").exists())
                .andExpect(jsonPath("$.paths['/api/cartoes/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/cartoes/{id}/status']").exists());
    }

    @Test
    void cadastrosDevemSerDocumentadosComo201() throws Exception {
        // Prova o @ResponseStatus(CREATED): sem ele, o Swagger mostraria 200 para os POST.
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/api/usuarios'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/cartoes'].post.responses['201']").exists());
    }

    @Test
    void deveServirAPaginaDoSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
