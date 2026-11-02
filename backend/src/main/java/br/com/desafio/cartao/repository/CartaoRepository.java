package br.com.desafio.cartao.repository;

import br.com.desafio.cartao.domain.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

// Acesso ao banco da tabela cartao. O Spring Data cria a implementação sozinho.
public interface CartaoRepository extends JpaRepository<Cartao, Long> {

    // Consulta derivada do nome: "SELECT ... WHERE numero_cartao = ?". Usada na regra do número único (6.6).
    boolean existsByNumeroCartao(Long numeroCartao);

    // nativeQuery: busca os cartões de VÁRIOS usuários numa consulta só.
    // Evita o problema "N+1": com 100 usuários, seria 1 consulta para os usuários + 100 para os cartões.
    // Atenção: uma lista vazia vira "IN ()", que é SQL inválido. Quem chama trata esse caso (6.6 e 7.2).
    @Query(value = """
            SELECT *
            FROM cartao
            WHERE usuario_id IN (:usuarioIds)
            ORDER BY id
            """, nativeQuery = true)
    List<Cartao> listarPorUsuarios(@Param("usuarioIds") Collection<Long> usuarioIds);

    // nativeQuery de escrita: um único DELETE apaga todos os cartões do usuário (usado no 7.3).
    // @Modifying: avisa o Spring Data que não é um SELECT. Devolve quantas linhas foram apagadas.
    // flushAutomatically: antes do DELETE, grava o que estiver pendente.
    // clearAutomatically: depois, limpa o cache do JPA, para ninguém enxergar um cartão que já foi apagado.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM cartao WHERE usuario_id = :usuarioId", nativeQuery = true)
    int removerPorUsuario(@Param("usuarioId") Long usuarioId);
}
