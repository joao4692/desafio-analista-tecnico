

package br.com.desafio.usuario.mapper;

import br.com.desafio.usuario.domain.Usuario;
import br.com.desafio.usuario.dto.UsuarioCreateRequest;
import br.com.desafio.usuario.dto.UsuarioResponse;
import org.springframework.stereotype.Component;

// Converte a entidade em DTO e o DTO em entidade.
// É escrito à mão, sem MapStruct: são poucos campos, e assim fica explícito e sem dependência extra.
// @Component: o Spring injeta o mapper no service (5.7).
@Component
public class UsuarioMapper {

    // Recebe o hash pronto: gerar o BCrypt é regra de negócio e fica no service.
    // A senha em texto que vem no request é ignorada de propósito.
    public Usuario paraEntidade(UsuarioCreateRequest request, String senhaHash) {
        return new Usuario(request.nome(), request.email(), senhaHash);
    }

    public UsuarioResponse paraResposta(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCriadoEm()
        );
    }
}
