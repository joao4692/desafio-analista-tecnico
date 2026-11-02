package br.com.desafio.cartao;

import br.com.desafio.TestcontainersConfiguration;
import br.com.desafio.cartao.domain.Cartao;
import br.com.desafio.cartao.domain.TipoCartao;
import br.com.desafio.cartao.repository.CartaoRepository;
import br.com.desafio.usuario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Teste de integração do cartão: a aplicação inteira e o Postgres real, sem mocks.
// Os usuários são criados pela API, como o front faria; os repositórios servem só para conferir e limpar o banco.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CartaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Limpa ANTES e DEPOIS de cada teste, sempre cartões → usuários (por causa da chave estrangeira).
    // O "depois" protege as outras classes: o Spring reaproveita o mesmo contexto e o mesmo banco
    // entre os testes de integração. Se sobrassem cartões aqui, o deleteAll() de usuários
    // do UsuarioIntegrationTest falharia.
    @BeforeEach
    void limparAntes() {
        limparBanco();
    }

    @AfterEach
    void limparDepois() {
        limparBanco();
    }

    @Test
    void adicionarDeveGravarCartaoAtivoParaUsuarioExistente() throws Exception {
        Long ana = criarUsuario("ana@email.com");

        Long id = criarCartao(1001L, ana);

        Cartao noBanco = cartaoRepository.findById(id).orElseThrow();
        assertThat(noBanco.isAtivo()).isTrue();
        assertThat(noBanco.getUsuarioId()).isEqualTo(ana);
        assertThat(noBanco.getTipoCartao()).isEqualTo(TipoCartao.ESTUDANTE);
    }

    @Test
    void adicionarParaUsuarioInexistenteDeveRetornar404SemGravar() throws Exception {
        mockMvc.perform(post("/api/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCartao(1001L, 999_999L)))
                .andExpect(status().isNotFound());

        assertThat(cartaoRepository.count()).isZero();
    }

    @Test
    void numeroRepetidoDeveRetornar409SemGravar() throws Exception {
        Long ana = criarUsuario("ana@email.com");
        criarCartao(1001L, ana);

        mockMvc.perform(post("/api/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCartao(1001L, ana)))
                .andExpect(status().isConflict());

        assertThat(cartaoRepository.count()).isEqualTo(1);
    }

    @Test
    void listarDeveFiltrarPorUsuario() throws Exception {
        Long ana = criarUsuario("ana@email.com");
        Long bia = criarUsuario("bia@email.com");
        criarCartao(1001L, ana);
        criarCartao(1002L, bia);
        criarCartao(1003L, ana);

        // Com filtro: só os da Ana, em ordem de id (nativeQuery do 6.3 no banco real).
        mockMvc.perform(get("/api/cartoes").param("usuarioId", ana.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].numeroCartao").value(1001))
                .andExpect(jsonPath("$[1].numeroCartao").value(1003));

        // Sem filtro: todos.
        mockMvc.perform(get("/api/cartoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void alterarStatusDevePersistirNoBanco() throws Exception {
        Long id = criarCartao(1001L, criarUsuario("ana@email.com"));

        mockMvc.perform(patch("/api/cartoes/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false));
        // Lê do banco: prova que o dirty checking gravou sem save().
        assertThat(cartaoRepository.findById(id).orElseThrow().isAtivo()).isFalse();

        mockMvc.perform(patch("/api/cartoes/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": true}
                                """))
                .andExpect(status().isOk());
        assertThat(cartaoRepository.findById(id).orElseThrow().isAtivo()).isTrue();
    }

    @Test
    void removerDeveApagarDoBancoEDepoisRetornar404() throws Exception {
        Long id = criarCartao(1001L, criarUsuario("ana@email.com"));

        mockMvc.perform(delete("/api/cartoes/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(cartaoRepository.existsById(id)).isFalse();
        mockMvc.perform(delete("/api/cartoes/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void removerUsuarioComCartoesAindaRetorna409() throws Exception {
        // Comportamento TEMPORÁRIO, registrado de propósito: a chave estrangeira da V2 barra a exclusão.
        // No 7.3, o evento passa a apagar os cartões antes, e no 7.4 este teste é trocado
        // por "remover o usuário remove os cartões".
        Long ana = criarUsuario("ana@email.com");
        criarCartao(1001L, ana);

        mockMvc.perform(delete("/api/usuarios/{id}", ana))
                .andExpect(status().isConflict());

        assertThat(usuarioRepository.existsById(ana)).isTrue();
        assertThat(cartaoRepository.count()).isEqualTo(1);
    }

    private void limparBanco() {
        cartaoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private Long criarUsuario(String email) throws Exception {
        String resposta = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Usuário Teste", "email": "%s", "senha": "senha123"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number id = JsonPath.read(resposta, "$.id");
        return id.longValue();
    }

    private Long criarCartao(long numero, Long usuarioId) throws Exception {
        String resposta = mockMvc.perform(post("/api/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCartao(numero, usuarioId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number id = JsonPath.read(resposta, "$.id");
        return id.longValue();
    }

    private String corpoCartao(long numero, Long usuarioId) {
        return """
                {"numeroCartao": %d, "nome": "Cartão %d", "tipoCartao": "ESTUDANTE", "usuarioId": %d}
                """.formatted(numero, numero, usuarioId);
    }
}
