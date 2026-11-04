package br.com.desafio.usuario.api;

// Contrato público do módulo de usuário: é a ÚNICA porta que outros módulos podem usar.
// O módulo de cartão depende desta interface, e não da entidade Usuario nem do UsuarioRepository.
// Se o usuário virar um microsserviço, basta trocar a implementação por uma chamada HTTP,
// sem mexer em nada do cartão.
public interface UsuarioConsulta {

    // Usado pelo CartaoService (6.6): só se cadastra cartão para um usuário que existe.
    boolean existe(Long usuarioId);
}
