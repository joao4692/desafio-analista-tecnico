package br.com.desafio.usuario.controller;

import br.com.desafio.usuario.dto.UsuarioCreateRequest;
import br.com.desafio.usuario.dto.UsuarioResponse;
import br.com.desafio.usuario.dto.UsuarioUpdateRequest;
import br.com.desafio.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Porta de entrada HTTP do módulo de usuário.
// Só traduz HTTP ↔ service: não tem regra de negócio e não trata erro (isso é do GlobalExceptionHandler).
// @Tag agrupa as rotas no Swagger; cada @Operation vira o título e a explicação de uma rota.
@Tag(name = "Usuários", description = "Cadastro de usuários. Cada usuário vem com a lista dos seus cartões.")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Lista os usuários", description = "Ordenados por id, cada um com os seus cartões.")
    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Busca um usuário pelo id", description = "Retorna 404 se o usuário não existir.")
    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Cadastra um usuário",
            description = "A senha é obrigatória e é gravada com BCrypt. O e-mail é salvo em minúsculas "
                    + "e é único: retorna 409 se já existir.")
    // O @ResponseStatus aqui só informa o 201 ao Swagger. Quem define o status de verdade é o ResponseEntity.created.
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse criado = service.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @Operation(summary = "Altera um usuário",
            description = "A senha é opcional: vazia ou ausente mantém a atual. O e-mail continua único (409).")
    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest request) {
        return service.atualizar(id, request);
    }

    @Operation(summary = "Remove um usuário",
            description = "Remove também todos os cartões do usuário, na mesma transação. Retorna 404 se não existir.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
