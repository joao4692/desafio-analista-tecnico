package br.com.desafio.usuario.repository;

import br.com.desafio.usuario.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Acesso ao banco da tabela usuario. É uma interface: o Spring Data cria a implementação sozinho.
// JpaRepository<Usuario, Long> já entrega save, findById, findAll, deleteById...
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Consulta derivada do nome do método: o Spring Data gera "SELECT ... WHERE email = ?".
    // Usada no cadastro (POST): qualquer usuário com esse e-mail já é conflito.
    boolean existsByEmail(String email);

    // nativeQuery: SQL puro do Postgres, escrito à mão.
    // Usada na alteração (PUT): o usuário pode manter o próprio e-mail,
    // então o conflito só existe se OUTRO id já usa esse e-mail.
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM usuario
                WHERE email = :email
                  AND id <> :id
            )
            """, nativeQuery = true)
    boolean existeEmailEmOutroUsuario(@Param("email") String email, @Param("id") Long id);
}
