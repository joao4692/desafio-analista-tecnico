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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;

// Camada de serviço: concentra as regras de negócio do usuário.
// O controller só recebe a requisição; quem decide se pode ou não é esta classe.
// Do módulo de cartão, só conhece o contrato CartaoConsulta (pacote cartao.api).
@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final CartaoConsulta cartaoConsulta;
    // Publica eventos para outros módulos sem conhecê-los (7.3).
    private final ApplicationEventPublisher eventPublisher;

    // Injeção pelo construtor (sem @Autowired): as dependências ficam explícitas,
    // e no teste basta passar os mocks direto no "new".
    public UsuarioService(UsuarioRepository repository, UsuarioMapper mapper,
                          PasswordEncoder passwordEncoder, CartaoConsulta cartaoConsulta,
                          ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.cartaoConsulta = cartaoConsulta;
        this.eventPublisher = eventPublisher;
    }

    // 2 consultas no total, seja 1 usuário ou 1000: uma para os usuários e UMA para todos os cartões deles.
    // Buscar os cartões de usuário em usuário faria 1 + N consultas (o problema "N+1").
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        List<Usuario> usuarios = repository.findAll(Sort.by("id"));

        List<Long> ids = usuarios.stream().map(Usuario::getId).toList();
        Map<Long, List<CartaoResumo>> cartoesPorUsuario = cartaoConsulta.listarPorUsuarios(ids);

        // getOrDefault: um usuário sem cartões não aparece no mapa e recebe uma lista vazia.
        return usuarios.stream()
                .map(usuario -> mapper.paraResposta(usuario,
                        cartoesPorUsuario.getOrDefault(usuario.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return respostaComCartoes(id, buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCreateRequest request) {
        String email = normalizarEmail(request.email());

        // A checagem vem ANTES do BCrypt, que é lento de propósito: não vale gastar tempo com um cadastro que vai falhar.
        if (repository.existsByEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado: " + email);
        }

        // Copia o request com o nome e o e-mail limpos, para o mapper montar a entidade já normalizada.
        var dados = new UsuarioCreateRequest(request.nome().trim(), email, request.senha());
        Usuario usuario = mapper.paraEntidade(dados, passwordEncoder.encode(request.senha()));

        // Usuário novo ainda não tem cartões: nem precisa consultar o módulo de cartão.
        return mapper.paraResposta(repository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = buscarEntidade(id);
        String email = normalizarEmail(request.email());

        // nativeQuery do 5.4: o usuário pode manter o próprio e-mail; o conflito é só com OUTRO id.
        if (repository.existeEmailEmOutroUsuario(email, id)) {
            throw new ConflitoException("E-mail já cadastrado: " + email);
        }

        usuario.atualizar(request.nome().trim(), email);

        // Senha opcional: null, vazia ou só espaços mantém a senha atual.
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.alterarSenha(passwordEncoder.encode(request.senha()));
        }

        // Sem save(): a entidade veio do banco dentro desta transação, e o Hibernate
        // grava as mudanças sozinho no commit (o "dirty checking").
        return respostaComCartoes(id, usuario);
    }

    @Transactional
    public void remover(Long id) {
        Usuario usuario = buscarEntidade(id);

        // 1º avisa os outros módulos (o de cartão apaga os cartões deste usuário),
        // 2º apaga o usuário. Nesta ordem, por causa da chave estrangeira da V2.
        // Tudo dentro desta transação: se qualquer parte falhar, nada é apagado.
        eventPublisher.publishEvent(new UsuarioRemovidoEvent(id));
        repository.delete(usuario);
    }

    // Busca reaproveitada por buscar, atualizar e remover: o 404 fica num lugar só.
    private Usuario buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));
    }

    // Monta a resposta de UM usuário com os cartões dele, usando a mesma consulta da listagem.
    private UsuarioResponse respostaComCartoes(Long id, Usuario usuario) {
        List<CartaoResumo> cartoes = cartaoConsulta.listarPorUsuarios(List.of(id)).getOrDefault(id, List.of());
        return mapper.paraResposta(usuario, cartoes);
    }

    // Locale.ROOT: o toLowerCase não depende do idioma do servidor (em turco, por exemplo, o "I" vira "ı").
    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
