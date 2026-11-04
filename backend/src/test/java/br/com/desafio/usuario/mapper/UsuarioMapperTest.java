package br.com.desafio.usuario.mapper;

import br.com.desafio.cartao.api.CartaoResumo;
import br.com.desafio.usuario.domain.Usuario;
import br.com.desafio.usuario.dto.UsuarioCreateRequest;
import br.com.desafio.usuario.dto.UsuarioResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Teste unitário puro: sem Spring e sem banco, roda em milissegundos.
class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapper();

    @Test
    void paraEntidadeDeveUsarOHashENaoASenhaDigitada() {
        var request = new UsuarioCreateRequest("Ana", "ana@email.com", "senha123");

        Usuario usuario = mapper.paraEntidade(request, "hash-bcrypt");

        assertThat(usuario.getNome()).isEqualTo("Ana");
        assertThat(usuario.getEmail()).isEqualTo("ana@email.com");
        assertThat(usuario.getSenha()).isEqualTo("hash-bcrypt");
    }

    @Test
    void paraRespostaDeveCopiarOsDadosDoUsuario() {
        Usuario usuario = new Usuario("Ana", "ana@email.com", "hash-bcrypt");
        LocalDateTime criadoEm = LocalDateTime.of(2026, 11, 2, 10, 0);
        // id e criadoEm normalmente vêm do banco. Aqui eles são preenchidos à mão para testar sem banco.
        ReflectionTestUtils.setField(usuario, "id", 1L);
        ReflectionTestUtils.setField(usuario, "criadoEm", criadoEm);

        UsuarioResponse resposta = mapper.paraResposta(usuario);

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.nome()).isEqualTo("Ana");
        assertThat(resposta.email()).isEqualTo("ana@email.com");
        assertThat(resposta.criadoEm()).isEqualTo(criadoEm);
    }

    @Test
    void respostaNuncaDeveTerCampoDeSenha() {
        // Trava de segurança: se alguém puser "senha" no UsuarioResponse no futuro, este teste falha.
        assertThat(UsuarioResponse.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .doesNotContain("senha");
    }

    @Test
    void paraRespostaComCartoesDeveIncluirALista() {
        Usuario usuario = new Usuario("Ana", "ana@email.com", "hash-bcrypt");
        var cartao = new CartaoResumo(10L, 1001L, "Cartão da Ana", true, "COMUM");

        assertThat(mapper.paraResposta(usuario, List.of(cartao)).cartoes()).containsExactly(cartao);
    }

    @Test
    void paraRespostaSemCartoesDeveTrazerListaVaziaENaoNull() {
        Usuario usuario = new Usuario("Ana", "ana@email.com", "hash-bcrypt");

        assertThat(mapper.paraResposta(usuario).cartoes()).isNotNull().isEmpty();
    }
}
