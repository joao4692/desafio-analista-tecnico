package br.com.desafio.cartao.mapper;

import br.com.desafio.cartao.domain.Cartao;
import br.com.desafio.cartao.dto.CartaoCreateRequest;
import br.com.desafio.cartao.dto.CartaoResponse;
import org.springframework.stereotype.Component;

// Converte o cartão entre entidade e DTO. Escrito à mão, no mesmo padrão do UsuarioMapper.
// Não tem método para o CartaoStatusRequest: no PATCH, o service chama direto cartao.alterarStatus(...).
@Component
public class CartaoMapper {

    // O status não vem do request: o construtor da entidade cria o cartão já ativo.
    public Cartao paraEntidade(CartaoCreateRequest request) {
        return new Cartao(
                request.numeroCartao(),
                request.nome(),
                request.tipoCartao(),
                request.usuarioId()
        );
    }

    public CartaoResponse paraResposta(Cartao cartao) {
        return new CartaoResponse(
                cartao.getId(),
                cartao.getNumeroCartao(),
                cartao.getNome(),
                cartao.isAtivo(),
                cartao.getTipoCartao(),
                cartao.getUsuarioId()
        );
    }
}
