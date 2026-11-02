package br.com.desafio.usuario.service;

import br.com.desafio.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// Fica no mesmo pacote da implementação: só assim o teste enxerga a classe, que não é pública.
@ExtendWith(MockitoExtension.class)
class UsuarioConsultaImplTest {

    @Mock
    private UsuarioRepository repository;

    @Test
    void existeDeveResponderConformeOBanco() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.existsById(99L)).thenReturn(false);

        UsuarioConsultaImpl consulta = new UsuarioConsultaImpl(repository);

        assertThat(consulta.existe(1L)).isTrue();
        assertThat(consulta.existe(99L)).isFalse();
    }
}
