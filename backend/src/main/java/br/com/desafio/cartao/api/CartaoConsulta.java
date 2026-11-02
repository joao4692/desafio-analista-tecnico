package br.com.desafio.cartao.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// Contrato público do módulo de cartão: é a única porta que o módulo de usuário pode usar para ler cartões.
// É o mesmo papel do UsuarioConsulta (6.5), no sentido contrário.
public interface CartaoConsulta {

    // Devolve os cartões agrupados pelo id do usuário, buscados numa ÚNICA consulta ao banco.
    // Um usuário sem cartões não aparece no mapa; quem chama usa getOrDefault(id, List.of()).
    Map<Long, List<CartaoResumo>> listarPorUsuarios(Collection<Long> usuarioIds);
}
