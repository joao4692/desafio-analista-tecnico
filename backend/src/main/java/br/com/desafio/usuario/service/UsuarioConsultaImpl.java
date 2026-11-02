package br.com.desafio.usuario.service;

import br.com.desafio.usuario.api.UsuarioConsulta;
import br.com.desafio.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Implementação do contrato, dentro do módulo de usuário.
// A classe não é "public" de propósito: fora deste pacote ninguém consegue usá-la diretamente.
// Os outros módulos recebem a interface UsuarioConsulta por injeção, e o Spring entrega esta classe.
@Component
class UsuarioConsultaImpl implements UsuarioConsulta {

    private final UsuarioRepository repository;

    UsuarioConsultaImpl(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(Long usuarioId) {
        return repository.existsById(usuarioId);
    }
}
