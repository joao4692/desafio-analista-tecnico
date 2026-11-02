package br.com.desafio.cartao.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Teste unitário puro da entidade: prova as regras que ficam dentro dela (nascer ativo e trocar o status).
class CartaoTest {

    @Test
    void cartaoNovoDeveNascerAtivo() {
        Cartao cartao = new Cartao(123456789L, "Cartão da Ana", TipoCartao.ESTUDANTE, 1L);

        assertThat(cartao.isAtivo()).isTrue();
        assertThat(cartao.getTipoCartao()).isEqualTo(TipoCartao.ESTUDANTE);
        assertThat(cartao.getUsuarioId()).isEqualTo(1L);
    }

    @Test
    void alterarStatusDeveInativarEReativar() {
        Cartao cartao = new Cartao(123456789L, "Cartão da Ana", TipoCartao.COMUM, 1L);

        cartao.alterarStatus(false);
        assertThat(cartao.isAtivo()).isFalse();

        cartao.alterarStatus(true);
        assertThat(cartao.isAtivo()).isTrue();
    }
}
