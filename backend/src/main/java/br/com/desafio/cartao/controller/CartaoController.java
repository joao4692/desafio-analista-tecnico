package br.com.desafio.cartao.controller;

import br.com.desafio.cartao.dto.CartaoCreateRequest;
import br.com.desafio.cartao.dto.CartaoResponse;
import br.com.desafio.cartao.dto.CartaoStatusRequest;
import br.com.desafio.cartao.service.CartaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Porta de entrada HTTP do módulo de cartão. Só traduz HTTP ↔ service, no mesmo padrão do UsuarioController.
@Tag(name = "Cartões", description = "Cartões de ônibus dos usuários: cadastro, ativação/inativação e remoção.")
@RestController
@RequestMapping("/api/cartoes")
public class CartaoController {

    private final CartaoService service;

    public CartaoController(CartaoService service) {
        this.service = service;
    }

    @Operation(summary = "Lista os cartões",
            description = "Sem filtro, traz todos. Com ?usuarioId=, traz só os cartões daquele usuário.")
    @GetMapping
    public List<CartaoResponse> listar(
            @Parameter(description = "Opcional: filtra pelos cartões de um usuário")
            @RequestParam(required = false) Long usuarioId) {
        return service.listar(usuarioId);
    }

    @Operation(summary = "Adiciona um cartão a um usuário",
            description = "Todo cartão nasce ativo. O usuário precisa existir (404) e o número é único (409). "
                    + "Tipos aceitos: COMUM, ESTUDANTE e TRABALHADOR.")
    // Só informa o 201 ao Swagger; o status de verdade vem do ResponseEntity.created.
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<CartaoResponse> adicionar(@Valid @RequestBody CartaoCreateRequest request) {
        CartaoResponse criado = service.adicionar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @Operation(summary = "Ativa ou inativa um cartão",
            description = "Corpo { \"status\": true } ativa e { \"status\": false } inativa. Retorna 404 se o cartão não existir.")
    @PatchMapping("/{id}/status")
    public CartaoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody CartaoStatusRequest request) {
        return service.alterarStatus(id, request);
    }

    @Operation(summary = "Remove um cartão", description = "Retorna 404 se o cartão não existir.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
