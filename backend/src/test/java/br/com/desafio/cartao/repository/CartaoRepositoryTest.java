package br.com.desafio.cartao.repository;

import br.com.desafio.TestcontainersConfiguration;
import br.com.desafio.cartao.domain.Cartao;
import br.com.desafio.cartao.domain.TipoCartao;
import br.com.desafio.usuario.domain.Usuario;
import br.com.desafio.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Mesmo modelo do UsuarioRepositoryTest: só a camada de banco, num Postgres real,
// com cada teste desfeito no final.
@DataJpaTest
@Import(TestcontainersConfiguration.class)
class CartaoRepositoryTest {

    @Autowired
    private CartaoRepository repository;

    // Usado SÓ para preparar os dados: a chave estrangeira exige que o usuário exista.
    // O código de produção do cartão não usa o UsuarioRepository.
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Roda uma vez para cada valor do enum: se o TipoCartao e o CHECK ck_cartao_tipo da V2
    // divergirem (ex.: um erro de digitação no enum), o banco recusa e o teste falha.
    @ParameterizedTest
    @EnumSource(TipoCartao.class)
    void bancoDeveAceitarTodosOsTiposDoEnum(TipoCartao tipo) {
        Long ana = criarUsuario("ana@email.com");

        Cartao salvo = repository.saveAndFlush(new Cartao(1001L, "Cartão " + tipo, tipo, ana));

        assertThat(repository.findById(salvo.getId())).get()
                .extracting(Cartao::getTipoCartao).isEqualTo(tipo);
    }

    @Test
    void existsByNumeroCartaoDeveIndicarSeONumeroJaExiste() {
        Long ana = criarUsuario("ana@email.com");
        repository.saveAndFlush(novoCartao(1001L, ana));

        assertThat(repository.existsByNumeroCartao(1001L)).isTrue();
        assertThat(repository.existsByNumeroCartao(9999L)).isFalse();
    }

    @Test
    void listarPorUsuariosDeveTrazerSoOsCartoesDosUsuariosPedidos() {
        Long ana = criarUsuario("ana@email.com");
        Long bia = criarUsuario("bia@email.com");
        Long caio = criarUsuario("caio@email.com");
        repository.saveAndFlush(novoCartao(1001L, ana));
        repository.saveAndFlush(novoCartao(1002L, bia));
        repository.saveAndFlush(novoCartao(1003L, ana));
        repository.saveAndFlush(novoCartao(1004L, caio));

        List<Cartao> cartoes = repository.listarPorUsuarios(List.of(ana, bia));

        // Ordenados por id, e nenhum do Caio.
        assertThat(cartoes).extracting(Cartao::getNumeroCartao).containsExactly(1001L, 1002L, 1003L);
    }

    @Test
    void removerPorUsuarioDeveApagarSoOsCartoesDoUsuario() {
        Long ana = criarUsuario("ana@email.com");
        Long bia = criarUsuario("bia@email.com");
        repository.saveAndFlush(novoCartao(1001L, ana));
        repository.saveAndFlush(novoCartao(1002L, ana));
        repository.saveAndFlush(novoCartao(1003L, bia));

        int removidos = repository.removerPorUsuario(ana);

        assertThat(removidos).isEqualTo(2);
        assertThat(repository.findAll()).extracting(Cartao::getUsuarioId).containsExactly(bia);
    }

    @Test
    void bancoDeveBarrarNumeroDuplicado() {
        Long ana = criarUsuario("ana@email.com");
        repository.saveAndFlush(novoCartao(1001L, ana));

        // Prova a constraint uk_cartao_numero da V2.
        assertThatThrownBy(() -> repository.saveAndFlush(novoCartao(1001L, ana)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void bancoDeveBarrarCartaoDeUsuarioInexistente() {
        // Prova a chave estrangeira fk_cartao_usuario: o usuário 999999 não existe.
        assertThatThrownBy(() -> repository.saveAndFlush(novoCartao(1001L, 999_999L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void bancoDeveBarrarExclusaoDeUsuarioQueTemCartoes() {
        Long ana = criarUsuario("ana@email.com");
        repository.saveAndFlush(novoCartao(1001L, ana));

        // Prova o "sem ON DELETE CASCADE" da V2: o usuário só pode sair depois dos cartões (Etapa 7).
        assertThatThrownBy(() -> {
            usuarioRepository.deleteById(ana);
            usuarioRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Long criarUsuario(String email) {
        return usuarioRepository.saveAndFlush(new Usuario("Nome Teste", email, "hash-bcrypt-falso")).getId();
    }

    private Cartao novoCartao(Long numero, Long usuarioId) {
        return new Cartao(numero, "Cartão " + numero, TipoCartao.COMUM, usuarioId);
    }
}
