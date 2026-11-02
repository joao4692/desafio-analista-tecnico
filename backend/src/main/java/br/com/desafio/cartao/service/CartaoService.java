package br.com.desafio.cartao.service;

import br.com.desafio.cartao.domain.Cartao;
import br.com.desafio.cartao.dto.CartaoCreateRequest;
import br.com.desafio.cartao.dto.CartaoResponse;
import br.com.desafio.cartao.dto.CartaoStatusRequest;
import br.com.desafio.cartao.mapper.CartaoMapper;
import br.com.desafio.cartao.repository.CartaoRepository;
import br.com.desafio.shared.exception.ConflitoException;
import br.com.desafio.shared.exception.RecursoNaoEncontradoException;
import br.com.desafio.usuario.api.UsuarioConsulta;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Camada de serviço do cartão: concentra as regras de negócio.
// Do módulo de usuário, só conhece o contrato UsuarioConsulta (pacote usuario.api).
@Service
public class CartaoService {

    private final CartaoRepository repository;
    private final CartaoMapper mapper;
    private final UsuarioConsulta usuarioConsulta;

    public CartaoService(CartaoRepository repository, CartaoMapper mapper, UsuarioConsulta usuarioConsulta) {
        this.repository = repository;
        this.mapper = mapper;
        this.usuarioConsulta = usuarioConsulta;
    }

    // usuarioId é opcional (vem do ?usuarioId= da URL).
    // Com o filtro, reaproveita a nativeQuery do 6.3, passando uma lista com um id só.
    // Um usuário sem cartões, ou que não existe, devolve lista vazia: um filtro sem resultado não é erro.
    @Transactional(readOnly = true)
    public List<CartaoResponse> listar(Long usuarioId) {
        List<Cartao> cartoes = (usuarioId == null)
                ? repository.findAll(Sort.by("id"))
                : repository.listarPorUsuarios(List.of(usuarioId));

        return cartoes.stream()
                .map(mapper::paraResposta)
                .toList();
    }

    @Transactional
    public CartaoResponse adicionar(CartaoCreateRequest request) {
        // 1ª regra: o dono precisa existir. É verificada pelo contrato, sem acessar o módulo de usuário por dentro.
        // A chave estrangeira da V2 também barraria, mas aqui o erro sai claro (404), e não um 409 genérico.
        if (!usuarioConsulta.existe(request.usuarioId())) {
            throw new RecursoNaoEncontradoException("Usuário " + request.usuarioId() + " não encontrado");
        }

        // 2ª regra: número único. A constraint uk_cartao_numero é o plano B para requisições simultâneas.
        if (repository.existsByNumeroCartao(request.numeroCartao())) {
            throw new ConflitoException("Número de cartão já cadastrado: " + request.numeroCartao());
        }

        // Copia o request com o nome sem espaços nas pontas, como no UsuarioService.
        var dados = new CartaoCreateRequest(
                request.numeroCartao(), request.nome().trim(), request.tipoCartao(), request.usuarioId());

        return mapper.paraResposta(repository.save(mapper.paraEntidade(dados)));
    }

    // PATCH /status: true ativa, false inativa. Sem save(): o dirty checking grava no commit, como no 5.7.
    @Transactional
    public CartaoResponse alterarStatus(Long id, CartaoStatusRequest request) {
        Cartao cartao = buscarEntidade(id);
        cartao.alterarStatus(request.status());
        return mapper.paraResposta(cartao);
    }

    @Transactional
    public void remover(Long id) {
        repository.delete(buscarEntidade(id));
    }

    private Cartao buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cartão " + id + " não encontrado"));
    }
}
