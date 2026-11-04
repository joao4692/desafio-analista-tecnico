package br.com.desafio.cartao.service;

import br.com.desafio.cartao.domain.Cartao;
import br.com.desafio.cartao.domain.TipoCartao;
import br.com.desafio.cartao.dto.CartaoCreateRequest;
import br.com.desafio.cartao.dto.CartaoResponse;
import br.com.desafio.cartao.dto.CartaoStatusRequest;
import br.com.desafio.cartao.mapper.CartaoMapper;
import br.com.desafio.cartao.repository.CartaoRepository;
import br.com.desafio.shared.exception.ConflitoException;
import br.com.desafio.shared.exception.RecursoNaoEncontradoException;
import br.com.desafio.usuario.api.UsuarioConsulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Teste unitário: o repositório e o contrato UsuarioConsulta são mocks, e o mapper é o real.
// Nem o banco nem o módulo de usuário participam: só as regras do cartão.
@ExtendWith(MockitoExtension.class)
class CartaoServiceTest {

    @Mock
    private CartaoRepository repository;

    @Mock
    private UsuarioConsulta usuarioConsulta;

    private CartaoService service;

    @BeforeEach
    void configurar() {
        service = new CartaoService(repository, new CartaoMapper(), usuarioConsulta);
    }

    @Test
    void listarSemFiltroDeveTrazerTodosOsCartoes() {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(
                novoCartao(1001L, 1L), novoCartao(1002L, 2L)));

        List<CartaoResponse> resposta = service.listar(null);

        assertThat(resposta).extracting(CartaoResponse::numeroCartao).containsExactly(1001L, 1002L);
        verify(repository, never()).listarPorUsuarios(any());
    }

    @Test
    void listarComFiltroDeveUsarAConsultaPorUsuario() {
        when(repository.listarPorUsuarios(List.of(1L))).thenReturn(List.of(novoCartao(1001L, 1L)));

        List<CartaoResponse> resposta = service.listar(1L);

        assertThat(resposta).extracting(CartaoResponse::usuarioId).containsExactly(1L);
        verify(repository, never()).findAll(any(Sort.class));
    }

    @Test
    void adicionarDeveSalvarCartaoAtivoComNomeLimpo() {
        var request = new CartaoCreateRequest(1001L, "  Cartão da Ana  ", TipoCartao.ESTUDANTE, 1L);
        when(usuarioConsulta.existe(1L)).thenReturn(true);
        when(repository.existsByNumeroCartao(1001L)).thenReturn(false);
        when(repository.save(any(Cartao.class))).thenAnswer(chamada -> chamada.getArgument(0));

        service.adicionar(request);

        ArgumentCaptor<Cartao> captor = ArgumentCaptor.forClass(Cartao.class);
        verify(repository).save(captor.capture());
        Cartao salvo = captor.getValue();
        assertThat(salvo.getNome()).isEqualTo("Cartão da Ana");
        assertThat(salvo.getUsuarioId()).isEqualTo(1L);
        assertThat(salvo.isAtivo()).isTrue();
    }

    @Test
    void adicionarParaUsuarioInexistenteDeveLancarNaoEncontradoSemSalvar() {
        var request = new CartaoCreateRequest(1001L, "Cartão", TipoCartao.COMUM, 99L);
        when(usuarioConsulta.existe(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.adicionar(request))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Usuário 99");

        verify(repository, never()).save(any());
    }

    @Test
    void adicionarComNumeroRepetidoDeveLancarConflitoSemSalvar() {
        var request = new CartaoCreateRequest(1001L, "Cartão", TipoCartao.COMUM, 1L);
        when(usuarioConsulta.existe(1L)).thenReturn(true);
        when(repository.existsByNumeroCartao(1001L)).thenReturn(true);

        assertThatThrownBy(() -> service.adicionar(request)).isInstanceOf(ConflitoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void alterarStatusDeveInativarOCartao() {
        Cartao cartao = novoCartao(1001L, 1L);
        when(repository.findById(10L)).thenReturn(Optional.of(cartao));

        CartaoResponse resposta = service.alterarStatus(10L, new CartaoStatusRequest(false));

        assertThat(cartao.isAtivo()).isFalse();
        assertThat(resposta.status()).isFalse();
    }

    @Test
    void alterarStatusDeCartaoInexistenteDeveLancarNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alterarStatus(99L, new CartaoStatusRequest(true)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Cartão 99");
    }

    @Test
    void removerDeveApagarCartaoExistente() {
        Cartao cartao = novoCartao(1001L, 1L);
        when(repository.findById(10L)).thenReturn(Optional.of(cartao));

        service.remover(10L);

        verify(repository).delete(cartao);
    }

    @Test
    void removerInexistenteDeveLancarNaoEncontradoSemApagar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(99L)).isInstanceOf(RecursoNaoEncontradoException.class);

        verify(repository, never()).delete(any());
    }

    private Cartao novoCartao(Long numero, Long usuarioId) {
        return new Cartao(numero, "Cartão " + numero, TipoCartao.COMUM, usuarioId);
    }
}
