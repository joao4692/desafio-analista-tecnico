package br.com.desafio.cartao.mapper;

import br.com.desafio.cartao.domain.Cartao;
import br.com.desafio.cartao.domain.TipoCartao;
import br.com.desafio.cartao.dto.CartaoCreateRequest;
import br.com.desafio.cartao.dto.CartaoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

// Teste unitário puro: sem Spring e sem banco.
class CartaoMapperTest {

    private final CartaoMapper mapper = new CartaoMapper();

    @Test
    void paraEntidadeDeveCopiarOsDadosECriarOCartaoAtivo() {
        var request = new CartaoCreateRequest(1001L, "Cartão da Ana", TipoCartao.ESTUDANTE, 1L);

        Cartao cartao = mapper.paraEntidade(request);

        assertThat(cartao.getNumeroCartao()).isEqualTo(1001L);
        assertThat(cartao.getNome()).isEqualTo("Cartão da Ana");
        assertThat(cartao.getTipoCartao()).isEqualTo(TipoCartao.ESTUDANTE);
        assertThat(cartao.getUsuarioId()).isEqualTo(1L);
        assertThat(cartao.isAtivo()).isTrue();
    }

    @Test
    void paraRespostaDeveCopiarOsDadosDoCartao() {
        Cartao cartao = new Cartao(1001L, "Cartão da Ana", TipoCartao.TRABALHADOR, 1L);
        cartao.alterarStatus(false);
        // O id normalmente vem do banco; aqui é preenchido à mão para testar sem banco.
        ReflectionTestUtils.setField(cartao, "id", 10L);

        CartaoResponse resposta = mapper.paraResposta(cartao);

        assertThat(resposta.id()).isEqualTo(10L);
        assertThat(resposta.numeroCartao()).isEqualTo(1001L);
        assertThat(resposta.nome()).isEqualTo("Cartão da Ana");
        assertThat(resposta.status()).isFalse();
        assertThat(resposta.tipoCartao()).isEqualTo(TipoCartao.TRABALHADOR);
        assertThat(resposta.usuarioId()).isEqualTo(1L);
    }
}
