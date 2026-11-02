-- Migração V1: cria a tabela de usuários.
-- O Flyway lê a versão pelo nome do arquivo (V1, V2...) e roda cada uma só uma vez.
-- Regra de ouro: depois de aplicada, NUNCA edite esta migração. Para mudar algo, crie uma V2, V3...
-- Se a V1 for editada, o Flyway acusa erro de checksum ao subir.

CREATE TABLE usuario (
    -- BIGSERIAL: o próprio Postgres gera o id (sequência). Na entidade isso vira GenerationType.IDENTITY.
    id         BIGSERIAL    PRIMARY KEY,
    nome       VARCHAR(120) NOT NULL,
    email      VARCHAR(160) NOT NULL,
    -- Guarda o hash BCrypt (60 caracteres), nunca a senha em texto puro.
    -- O tamanho 100 deixa folga para trocar de algoritmo no futuro.
    senha      VARCHAR(100) NOT NULL,
    criado_em  TIMESTAMP    NOT NULL,

    -- O e-mail é único também no banco, e não só no service: se duas requisições
    -- chegarem ao mesmo tempo, o banco ainda barra o duplicado.
    -- O nome da constraint é explícito para facilitar a leitura do erro no GlobalExceptionHandler (5.6).
    CONSTRAINT uk_usuario_email UNIQUE (email)
);
