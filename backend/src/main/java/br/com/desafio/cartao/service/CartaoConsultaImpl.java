package br.com.desafio.cartao.service;

import br.com.desafio.cartao.api.CartaoConsulta;
import br.com.desafio.cartao.api.CartaoResumo;
import br.com.desafio.cartao.domain.Cartao;
import br.com.desafio.cartao.repository.CartaoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Implementação do contrato, dentro do módulo de cartão. Não é "public", como o UsuarioConsultaImpl:
// os outros módulos só enxergam a interface.
@Component
class CartaoConsultaImpl implements CartaoConsulta {

    private final CartaoRepository repository;

    CartaoConsultaImpl(CartaoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<CartaoResumo>> listarPorUsuarios(Collection<Long> usuarioIds) {
        // Aviso do 6.3: uma lista vazia viraria "IN ()", que é SQL inválido. Então nem vai ao banco.
        if (usuarioIds.isEmpty()) {
            return Map.of();
        }

        // Uma consulta só (a nativeQuery do 6.3), e o agrupamento por usuário é feito em memória.
        // Dentro de cada grupo, a ordem por id que veio do banco é mantida.
        return repository.listarPorUsuarios(usuarioIds).stream()
                .collect(Collectors.groupingBy(
                        Cartao::getUsuarioId,
                        Collectors.mapping(this::paraResumo, Collectors.toList())));
    }

    private CartaoResumo paraResumo(Cartao cartao) {
        return new CartaoResumo(
                cartao.getId(),
                cartao.getNumeroCartao(),
                cartao.getNome(),
                cartao.isAtivo(),
                cartao.getTipoCartao().name()
        );
    }
}
