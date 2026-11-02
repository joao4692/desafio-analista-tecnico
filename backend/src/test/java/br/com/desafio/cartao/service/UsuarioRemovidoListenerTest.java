package br.com.desafio.cartao.service;

import br.com.desafio.cartao.repository.CartaoRepository;
import br.com.desafio.usuario.api.UsuarioRemovidoEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

// Teste unitário do listener: recebe o evento e manda apagar os cartões do usuário certo.
// A parte da transação (tudo junto ou nada) é provada com o banco real, no teste de integração.
@ExtendWith(MockitoExtension.class)
class UsuarioRemovidoListenerTest {

    @Mock
    private CartaoRepository repository;

    @Test
    void deveApagarOsCartoesDoUsuarioRemovido() {
        UsuarioRemovidoListener listener = new UsuarioRemovidoListener(repository);

        listener.aoRemoverUsuario(new UsuarioRemovidoEvent(1L));

        verify(repository).removerPorUsuario(1L);
    }
}
