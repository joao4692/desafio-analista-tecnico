package br.com.desafio.usuario.service;

import br.com.desafio.shared.exception.ConflitoException;
import br.com.desafio.shared.exception.RecursoNaoEncontradoException;
import br.com.desafio.usuario.domain.Usuario;
import br.com.desafio.usuario.dto.UsuarioCreateRequest;
import br.com.desafio.usuario.dto.UsuarioResponse;
import br.com.desafio.usuario.dto.UsuarioUpdateRequest;
import br.com.desafio.usuario.mapper.UsuarioMapper;
import br.com.desafio.usuario.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

// Camada de serviço: concentra as regras de negócio do usuário.
// O controller só recebe a requisição; quem decide se pode ou não é esta classe.
@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;

    // Injeção pelo construtor (sem @Autowired): as dependências ficam explícitas,
    // e no teste basta passar os mocks direto no "new".
    public UsuarioService(UsuarioRepository repository, UsuarioMapper mapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    // readOnly = true: avisa o Hibernate que nada será alterado, e ele pula a checagem de mudanças.
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        // Ordem fixa por id: a lista no front não "pula" de posição entre uma chamada e outra.
        return repository.findAll(Sort.by("id")).stream()
                .map(mapper::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return mapper.paraResposta(buscarEntidade(id));
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
        // grava as mudanças sozinho no commit (o "dirty checking"). O 5.9 prova isso com o banco real.
        return mapper.paraResposta(usuario);
    }

    @Transactional
    public void remover(Long id) {
        Usuario usuario = buscarEntidade(id);
        // No Passo 7.3, antes desta linha, entra o UsuarioRemovidoEvent para apagar os cartões do usuário.
        repository.delete(usuario);
    }

    // Busca reaproveitada por buscar, atualizar e remover: o 404 fica num lugar só.
    private Usuario buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));
    }

    // Locale.ROOT: o toLowerCase não depende do idioma do servidor (em turco, por exemplo, o "I" vira "ı").
    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
