package br.com.desafio.cartao.service;

import br.com.desafio.cartao.repository.CartaoRepository;
import br.com.desafio.usuario.api.UsuarioRemovidoEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// O módulo de cartão "escuta" a remoção de usuários e apaga os cartões deles.
// O módulo de usuário só publica o evento: não sabe que o cartão existe, nem o que ele faz com o aviso.
// Não é "public", como as outras implementações internas: só o Spring a usa.
@Component
class UsuarioRemovidoListener {

    private final CartaoRepository repository;

    UsuarioRemovidoListener(CartaoRepository repository) {
        this.repository = repository;
    }

    // @EventListener é síncrono: roda na hora do publishEvent, na mesma thread e na MESMA transação
    // do UsuarioService.remover. Se algo falhar aqui ou no DELETE do usuário, tudo é desfeito junto.
    // MANDATORY: exige uma transação já aberta. Se alguém publicar o evento fora de uma,
    // o método falha na hora, em vez de apagar os cartões sem proteção.
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void aoRemoverUsuario(UsuarioRemovidoEvent evento) {
        // Um único DELETE para todos os cartões do usuário: a nativeQuery do 6.3.
        repository.removerPorUsuario(evento.usuarioId());
    }
}
