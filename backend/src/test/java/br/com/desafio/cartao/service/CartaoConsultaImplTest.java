package br.com.desafio.cartao.service;

import br.com.desafio.cartao.api.CartaoResumo;
import br.com.desafio.cartao.domain.Cartao;
import br.com.desafio.cartao.domain.TipoCartao;
import br.com.desafio.cartao.repository.CartaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Mesmo pacote da implementação: só assim o teste enxerga a classe, que não é pública.
@ExtendWith(MockitoExtension.class)
class CartaoConsultaImplTest {

    @Mock
    private CartaoRepository repository;

    private CartaoConsultaImpl consulta;

    @BeforeEach
    void configurar() {
        consulta = new CartaoConsultaImpl(repository);
    }

    @Test
    void deveAgruparOsCartoesPorUsuarioMantendoAOrdem() {
        when(repository.listarPorUsuarios(List.of(1L, 2L))).thenReturn(List.of(
                cartao(10L, 1001L, 1L),
                cartao(11L, 1002L, 2L),
                cartao(12L, 1003L, 1L)));

        Map<Long, List<CartaoResumo>> resultado = consulta.listarPorUsuarios(List.of(1L, 2L));

        assertThat(resultado.get(1L)).extracting(CartaoResumo::numeroCartao).containsExactly(1001L, 1003L);
        assertThat(resultado.get(2L)).extracting(CartaoResumo::numeroCartao).containsExactly(1002L);
    }

    @Test
    void deveConverterParaResumoComOTipoEmTexto() {
        Cartao inativo = cartao(10L, 1001L, 1L);
        inativo.alterarStatus(false);
        when(repository.listarPorUsuarios(List.of(1L))).thenReturn(List.of(inativo));

        CartaoResumo resumo = consulta.listarPorUsuarios(List.of(1L)).get(1L).getFirst();

        // Records comparam campo a campo: um equals só confere os 5 campos.
        assertThat(resumo).isEqualTo(new CartaoResumo(10L, 1001L, "Cartão 1001", false, "ESTUDANTE"));
    }

    @Test
    void listaVaziaNaoDeveIrAoBanco() {
        assertThat(consulta.listarPorUsuarios(List.of())).isEmpty();

        // Prova a proteção contra o "IN ()": o repositório nem foi chamado.
        verifyNoInteractions(repository);
    }

    private Cartao cartao(Long id, Long numero, Long usuarioId) {
        Cartao cartao = new Cartao(numero, "Cartão " + numero, TipoCartao.ESTUDANTE, usuarioId);
        ReflectionTestUtils.setField(cartao, "id", id);
        return cartao;
    }
}
