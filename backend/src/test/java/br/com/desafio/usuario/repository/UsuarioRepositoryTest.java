package br.com.desafio.usuario.repository;

import br.com.desafio.TestcontainersConfiguration;
import br.com.desafio.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// @DataJpaTest sobe só a camada de banco (JPA + Flyway), sem controllers, e por isso é mais rápido.
// Cada teste roda numa transação que é desfeita no final, então um teste não suja o outro.
// O @Import traz o Postgres real do Testcontainers, o mesmo usado no contextLoads.
@DataJpaTest
@Import(TestcontainersConfiguration.class)
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    @Test
    void deveGerarIdEDataDeCriacaoAoSalvar() {
        Usuario salvo = repository.saveAndFlush(novoUsuario("ana@email.com"));

        // O id vem do BIGSERIAL e a data vem do @PrePersist da entidade.
        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getCriadoEm()).isNotNull();
    }

    @Test
    void existsByEmailDeveIndicarSeOEmailJaEstaCadastrado() {
        repository.saveAndFlush(novoUsuario("ana@email.com"));

        assertThat(repository.existsByEmail("ana@email.com")).isTrue();
        assertThat(repository.existsByEmail("bia@email.com")).isFalse();
    }

    @Test
    void existeEmailEmOutroUsuarioDeveIgnorarOProprioUsuario() {
        Usuario ana = repository.saveAndFlush(novoUsuario("ana@email.com"));
        Usuario bia = repository.saveAndFlush(novoUsuario("bia@email.com"));

        // A Ana mantendo o próprio e-mail no PUT: não é conflito.
        assertThat(repository.existeEmailEmOutroUsuario("ana@email.com", ana.getId())).isFalse();
        // A Bia tentando usar o e-mail da Ana: é conflito.
        assertThat(repository.existeEmailEmOutroUsuario("ana@email.com", bia.getId())).isTrue();
    }

    @Test
    void bancoDeveBarrarEmailDuplicado() {
        repository.saveAndFlush(novoUsuario("ana@email.com"));

        // Prova a constraint uk_usuario_email da V1: mesmo que o service falhe, o banco barra.
        assertThatThrownBy(() -> repository.saveAndFlush(novoUsuario("ana@email.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // saveAndFlush manda o INSERT na hora; só assim a nativeQuery enxerga o registro.
    private Usuario novoUsuario(String email) {
        return new Usuario("Nome Teste", email, "hash-bcrypt-falso");
    }
}
