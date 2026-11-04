package br.com.desafio.usuario.mapper;

import br.com.desafio.cartao.api.CartaoResumo;
import br.com.desafio.usuario.domain.Usuario;
import br.com.desafio.usuario.dto.UsuarioCreateRequest;
import br.com.desafio.usuario.dto.UsuarioResponse;
import org.springframework.stereotype.Component;

import java.util.List;

// Converte a entidade em DTO e o DTO em entidade.
// É escrito à mão, sem MapStruct: são poucos campos, e assim fica explícito e sem dependência extra.
// @Component: o Spring injeta o mapper no service.
@Component
public class UsuarioMapper {

    // Recebe o hash pronto: gerar o BCrypt é regra de negócio e fica no service.
    // A senha em texto que vem no request é ignorada de propósito.
    public Usuario paraEntidade(UsuarioCreateRequest request, String senhaHash) {
        return new Usuario(request.nome(), request.email(), senhaHash);
    }

    // Usuário recém-criado: ainda não tem cartões, então a lista vai vazia (nunca null, para o front não precisar checar).
    public UsuarioResponse paraResposta(Usuario usuario) {
        return paraResposta(usuario, List.of());
    }

    // Os cartões chegam prontos: quem busca é o service, pelo contrato CartaoConsulta.
    public UsuarioResponse paraResposta(Usuario usuario, List<CartaoResumo> cartoes) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCriadoEm(),
                cartoes
        );
    }
}
