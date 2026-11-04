package br.com.desafio.usuario.api;

// Evento publicado pelo módulo de usuário ANTES de apagar um usuário.
// Faz parte do contrato público: outros módulos podem reagir a ele (o de cartão apaga os cartões).
// Leva só o id: quem escuta não precisa, nem deve, conhecer a entidade Usuario.
public record UsuarioRemovidoEvent(Long usuarioId) {
}
