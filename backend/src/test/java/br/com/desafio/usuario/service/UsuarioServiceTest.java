package br.com.desafio.usuario.service;

import br.com.desafio.cartao.api.CartaoConsulta;
import br.com.desafio.cartao.api.CartaoResumo;
import br.com.desafio.shared.exception.ConflitoException;
import br.com.desafio.shared.exception.RecursoNaoEncontradoException;
import br.com.desafio.usuario.api.UsuarioRemovidoEvent;
import br.com.desafio.usuario.domain.Usuario;
import br.com.desafio.usuario.dto.UsuarioCreateRequest;
import br.com.desafio.usuario.dto.UsuarioResponse;
import br.com.desafio.usuario.dto.UsuarioUpdateRequest;
import br.com.desafio.usuario.mapper.UsuarioMapper;
import br.com.desafio.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

// Teste unitário: o repositório e o BCrypt são MOCKS (dublês), sem banco e sem Spring.
// Assim só a regra de negócio do service é testada, e roda em milissegundos.
// O mapper é o real: é simples e já foi testado no 5.5.
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // Contrato do módulo de cartão (7.1). Quando não é configurado, o mock devolve um mapa vazio.
    @Mock
    private CartaoConsulta cartaoConsulta;

    // Publicador de eventos do Spring (7.3): aqui é um mock, para conferir O QUE foi publicado e QUANDO.
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UsuarioService service;

    @BeforeEach
    void configurar() {
        service = new UsuarioService(repository, new UsuarioMapper(), passwordEncoder, cartaoConsulta, eventPublisher);
    }

    @Test
    void listarDeveBuscarOsCartoesDeTodosOsUsuariosNumaChamadaSo() {
        Usuario ana = usuarioComId(1L, "Ana");
        Usuario bia = usuarioComId(2L, "Bia");
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(ana, bia));
        var cartaoDaAna = new CartaoResumo(10L, 1001L, "Cartão da Ana", true, "ESTUDANTE");
        when(cartaoConsulta.listarPorUsuarios(List.of(1L, 2L))).thenReturn(Map.of(1L, List.of(cartaoDaAna)));

        List<UsuarioResponse> resposta = service.listar();

        assertThat(resposta).extracting(UsuarioResponse::nome).containsExactly("Ana", "Bia");
        assertThat(resposta.get(0).cartoes()).containsExactly(cartaoDaAna);
        assertThat(resposta.get(1).cartoes()).isEmpty(); // a Bia não tem cartões: lista vazia, nunca null
        // UMA chamada para todos os usuários, e não uma por usuário (sem "N+1").
        verify(cartaoConsulta).listarPorUsuarios(List.of(1L, 2L));
        verifyNoMoreInteractions(cartaoConsulta);
    }

    @Test
    void buscarPorIdDeveTrazerOsCartoesDoUsuario() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioComId(1L, "Ana")));
        var cartaoDaAna = new CartaoResumo(10L, 1001L, "Cartão da Ana", true, "ESTUDANTE");
        when(cartaoConsulta.listarPorUsuarios(List.of(1L))).thenReturn(Map.of(1L, List.of(cartaoDaAna)));

        UsuarioResponse resposta = service.buscarPorId(1L);

        assertThat(resposta.cartoes()).containsExactly(cartaoDaAna);
    }

    @Test
    void criarDeveSalvarComSenhaCriptografadaEEmailNormalizado() {
        var request = new UsuarioCreateRequest("  Ana  ", " Ana@Email.COM ", "senha123");
        when(repository.existsByEmail("ana@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
        // O save devolve a própria entidade que recebeu, como o banco faria.
        when(repository.save(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));

        service.criar(request);

        // O ArgumentCaptor "pega" o Usuario que o service mandou salvar, para olhar dentro dele.
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        Usuario salvo = captor.getValue();
        assertThat(salvo.getNome()).isEqualTo("Ana");
        assertThat(salvo.getEmail()).isEqualTo("ana@email.com");
        assertThat(salvo.getSenha()).isEqualTo("hash-bcrypt"); // nunca "senha123"
    }

    @Test
    void criarComEmailJaCadastradoDeveLancarConflitoSemSalvar() {
        var request = new UsuarioCreateRequest("Ana", "ana@email.com", "senha123");
        when(repository.existsByEmail("ana@email.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request)).isInstanceOf(ConflitoException.class);

        verify(repository, never()).save(any());
        verify(passwordEncoder, never()).encode(any()); // nem gastou tempo com o BCrypt
    }

    @Test
    void buscarPorIdInexistenteDeveLancarNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    // Roda 3 vezes: senha null, "" e "   ". Nos três casos, a senha atual é mantida.
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void atualizarSemSenhaDeveManterASenhaAtual(String senhaVazia) {
        Usuario existente = new Usuario("Ana", "ana@email.com", "hash-antigo");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existeEmailEmOutroUsuario("ana.nova@email.com", 1L)).thenReturn(false);

        service.atualizar(1L, new UsuarioUpdateRequest("Ana Maria", "Ana.Nova@email.com", senhaVazia));

        assertThat(existente.getNome()).isEqualTo("Ana Maria");
        assertThat(existente.getEmail()).isEqualTo("ana.nova@email.com");
        assertThat(existente.getSenha()).isEqualTo("hash-antigo");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void atualizarComSenhaDeveCriptografarANova() {
        Usuario existente = new Usuario("Ana", "ana@email.com", "hash-antigo");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existeEmailEmOutroUsuario("ana@email.com", 1L)).thenReturn(false);
        when(passwordEncoder.encode("novaSenha")).thenReturn("hash-novo");

        service.atualizar(1L, new UsuarioUpdateRequest("Ana", "ana@email.com", "novaSenha"));

        assertThat(existente.getSenha()).isEqualTo("hash-novo");
    }

    @Test
    void atualizarComEmailDeOutroUsuarioDeveLancarConflitoSemAlterar() {
        Usuario existente = new Usuario("Ana", "ana@email.com", "hash");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existeEmailEmOutroUsuario("bia@email.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, new UsuarioUpdateRequest("Ana", "bia@email.com", null)))
                .isInstanceOf(ConflitoException.class);

        assertThat(existente.getEmail()).isEqualTo("ana@email.com"); // continua o mesmo
    }

    @Test
    void removerDevePublicarOEventoAntesDeApagar() {
        Usuario existente = new Usuario("Ana", "ana@email.com", "hash");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.remover(1L);

        // A ORDEM importa: primeiro os cartões saem (pelo evento), depois o usuário.
        // Invertido, a chave estrangeira da V2 barraria o DELETE do usuário.
        InOrder ordem = inOrder(eventPublisher, repository);
        ordem.verify(eventPublisher).publishEvent(new UsuarioRemovidoEvent(1L));
        ordem.verify(repository).delete(existente);
    }

    @Test
    void removerInexistenteDeveLancarNaoEncontradoSemPublicarNemApagar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(99L)).isInstanceOf(RecursoNaoEncontradoException.class);

        verifyNoInteractions(eventPublisher); // nenhum cartão foi apagado por engano
        verify(repository, never()).delete(any());
    }

    // O id normalmente vem do banco; aqui é preenchido à mão para testar sem banco.
    private Usuario usuarioComId(Long id, String nome) {
        Usuario usuario = new Usuario(nome, nome.toLowerCase() + "@email.com", "hash");
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }
}
