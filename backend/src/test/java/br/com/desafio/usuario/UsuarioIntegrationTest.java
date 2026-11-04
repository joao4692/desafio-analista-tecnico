package br.com.desafio.usuario;

import br.com.desafio.TestcontainersConfiguration;
import br.com.desafio.usuario.domain.Usuario;
import br.com.desafio.usuario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Teste de integração: @SpringBootTest sobe a aplicação inteira (controller → service → repository),
// e o @Import liga tudo num Postgres real do Testcontainers. Nada aqui é mock.
// O @AutoConfigureMockMvc permite fazer requisições HTTP sem abrir uma porta de rede.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UsuarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Usado só para CONFERIR o banco depois das requisições, e não para preparar os dados.
    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // A classe NÃO tem @Transactional, de propósito: cada requisição faz commit de verdade, como em produção.
    // Com @Transactional, o PUT nunca chegaria ao commit, e o teste do dirty checking não provaria nada.
    // Por isso o banco é limpo antes de cada teste.
    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    @Test
    void criarDeveGravarNoBancoComSenhaCriptografadaEEmailNormalizado() throws Exception {
        // Sem espaços nas pontas: o @Email do DTO já recusa com 400. As maiúsculas passam e o service normaliza.
        Long id = criarUsuario("Ana", "Ana@Email.COM", "senha123");

        Usuario noBanco = repository.findById(id).orElseThrow();
        assertThat(noBanco.getEmail()).isEqualTo("ana@email.com");
        assertThat(noBanco.getCriadoEm()).isNotNull(); // @PrePersist funcionou no banco real
        assertThat(noBanco.getSenha()).isNotEqualTo("senha123");
        // BCrypt de verdade: o hash confere com a senha original.
        assertThat(passwordEncoder.matches("senha123", noBanco.getSenha())).isTrue();
    }

    @Test
    void atualizarSemSenhaDevePersistirOsDadosEManterASenha() throws Exception {
        Long id = criarUsuario("Ana", "ana@email.com", "senha123");
        String hashOriginal = repository.findById(id).orElseThrow().getSenha();

        mockMvc.perform(put("/api/usuarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Ana Maria", "email": "ana.maria@email.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Maria"));

        // Lê de novo do banco: prova que o dirty checking gravou, mesmo sem save() no service.
        Usuario noBanco = repository.findById(id).orElseThrow();
        assertThat(noBanco.getNome()).isEqualTo("Ana Maria");
        assertThat(noBanco.getEmail()).isEqualTo("ana.maria@email.com");
        assertThat(noBanco.getSenha()).isEqualTo(hashOriginal);
    }

    @Test
    void emailDuplicadoMesmoComMaiusculasDeveRetornar409() throws Exception {
        criarUsuario("Ana", "ana@email.com", "senha123");

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Outra Ana", "email": "ANA@email.com", "senha": "senha456"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertThat(repository.count()).isEqualTo(1); // o segundo não foi gravado
    }

    @Test
    void removerDeveApagarDoBancoEDepoisRetornar404() throws Exception {
        Long id = criarUsuario("Ana", "ana@email.com", "senha123");

        mockMvc.perform(delete("/api/usuarios/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(repository.existsById(id)).isFalse();
        mockMvc.perform(get("/api/usuarios/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarDeveTrazerOsUsuariosEmOrdemDeIdSemSenha() throws Exception {
        criarUsuario("Ana", "ana@email.com", "senha123");
        criarUsuario("Bia", "bia@email.com", "senha123");

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Ana"))
                .andExpect(jsonPath("$[1].nome").value("Bia"))
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    // Cria pela API (e não pelo repository), para que os dados passem pelas mesmas regras de produção.
    // Devolve o id lido do JSON de resposta.
    private Long criarUsuario(String nome, String email, String senha) throws Exception {
        String corpo = """
                {"nome": "%s", "email": "%s", "senha": "%s"}
                """.formatted(nome, email, senha);

        String resposta = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number id = JsonPath.read(resposta, "$.id");
        return id.longValue();
    }
}
