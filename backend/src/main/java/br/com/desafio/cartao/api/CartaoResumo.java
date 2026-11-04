package br.com.desafio.cartao.api;

// A visão do cartão que o módulo de cartão aceita mostrar aos outros módulos.
// Fica em "api", e não em "dto", porque faz parte do contrato público.
// O tipo vai como String (ex.: "ESTUDANTE"), e não como o enum TipoCartao, para não expor o domínio do cartão.
// Não tem usuarioId: no contrato, os cartões já vêm agrupados por usuário.
public record CartaoResumo(
        Long id,
        Long numeroCartao,
        String nome,
        boolean status,
        String tipoCartao
) {
}
