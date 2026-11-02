package br.com.desafio.cartao.controller;

import br.com.desafio.cartao.dto.CartaoCreateRequest;
import br.com.desafio.cartao.dto.CartaoResponse;
import br.com.desafio.cartao.dto.CartaoStatusRequest;
import br.com.desafio.cartao.service.CartaoService;
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
@RestController
@RequestMapping("/api/cartoes")
public class CartaoController {

    private final CartaoService service;

    public CartaoController(CartaoService service) {
        this.service = service;
    }

    // ?usuarioId= é opcional (required = false): sem ele, vem null e o service lista todos.
    @GetMapping
    public List<CartaoResponse> listar(@RequestParam(required = false) Long usuarioId) {
        return service.listar(usuarioId);
    }

    @PostMapping
    public ResponseEntity<CartaoResponse> adicionar(@Valid @RequestBody CartaoCreateRequest request) {
        CartaoResponse criado = service.adicionar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(local).body(criado);
    }

    // PATCH, e não PUT: altera só uma parte do cartão (o status), sem mandar o cartão inteiro.
    @PatchMapping("/{id}/status")
    public CartaoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody CartaoStatusRequest request) {
        return service.alterarStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
