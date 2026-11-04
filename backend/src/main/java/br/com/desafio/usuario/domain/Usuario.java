package br.com.desafio.usuario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Entidade JPA: espelha a tabela "usuario" criada pela migração V1.
// Não tem setters soltos: quem muda o estado são os métodos com nome de negócio (atualizar, alterarSenha).
@Entity
@Table(name = "usuario")
public class Usuario {

    // IDENTITY: o id vem do BIGSERIAL do Postgres, e o banco gera o número no INSERT.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    // A unicidade do e-mail é garantida pela constraint uk_usuario_email (V1) e pelo service (5.7).
    @Column(nullable = false, length = 160)
    private String email;

    // Guarda sempre o hash BCrypt, nunca a senha digitada.
    @Column(nullable = false, length = 100)
    private String senha;

    // updatable = false: a data de criação é gravada uma vez e nunca muda num UPDATE.
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    // O JPA exige um construtor sem argumentos. Ele é protected para o resto do código
    // não criar um Usuario vazio por engano.
    protected Usuario() {
    }

    // Construtor usado pelo mapper (5.5): a senha que chega aqui já é o hash.
    public Usuario(String nome, String email, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.senha = senhaHash;
    }

    // Roda automaticamente antes do INSERT: a data é definida pelo sistema, e não por quem chama a API.
    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    // Usado no PUT: altera os dados cadastrais sem mexer na senha.
    public void atualizar(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    // Separado do atualizar() porque, no PUT, a senha é opcional (regra do 5.7).
    public void alterarSenha(String senhaHash) {
        this.senha = senhaHash;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
