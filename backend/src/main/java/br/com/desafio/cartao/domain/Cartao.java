package br.com.desafio.cartao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Entidade JPA: espelha a tabela "cartao" criada pela migração V2.
@Entity
@Table(name = "cartao")
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A unicidade é garantida pela constraint uk_cartao_numero (V2) e pelo service (6.6).
    @Column(name = "numero_cartao", nullable = false)
    private Long numeroCartao;

    @Column(nullable = false, length = 120)
    private String nome;

    // true = ativo. Mesmo nome da coluna e do JSON do PATCH ({ "status": true }).
    @Column(nullable = false)
    private boolean status;

    // EnumType.STRING grava o NOME ("ESTUDANTE"), e não a posição (1).
    // Com a posição, reordenar o enum trocaria o tipo de todos os cartões já gravados.
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cartao", nullable = false, length = 20)
    private TipoCartao tipoCartao;

    // Só o id do usuário, SEM @ManyToOne: o módulo de cartão não conhece a classe Usuario.
    // Quem confirma que o usuário existe é o contrato UsuarioConsulta (6.5).
    // updatable = false: um cartão nunca troca de dono.
    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    // Exigido pelo JPA; protected para o resto do código não criar um cartão vazio.
    protected Cartao() {
    }

    // Todo cartão novo nasce ativo: é a mesma regra do DEFAULT TRUE da V2, agora também no Java.
    public Cartao(Long numeroCartao, String nome, TipoCartao tipoCartao, Long usuarioId) {
        this.numeroCartao = numeroCartao;
        this.nome = nome;
        this.tipoCartao = tipoCartao;
        this.usuarioId = usuarioId;
        this.status = true;
    }

    // Usado pelo PATCH /api/cartoes/{id}/status: true ativa, false inativa.
    public void alterarStatus(boolean ativo) {
        this.status = ativo;
    }

    public Long getId() {
        return id;
    }

    public Long getNumeroCartao() {
        return numeroCartao;
    }

    public String getNome() {
        return nome;
    }

    // "isAtivo" lê melhor no código (if (cartao.isAtivo())) do que "getStatus".
    public boolean isAtivo() {
        return status;
    }

    public TipoCartao getTipoCartao() {
        return tipoCartao;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}
