-- Migração V2: cria a tabela de cartões de ônibus.
-- A V1 não é editada: cada mudança no banco é uma versão nova.

CREATE TABLE cartao (
                        id             BIGSERIAL    PRIMARY KEY,
    -- BIGINT: o número do cartão pode passar do limite do INTEGER (cerca de 2,1 bilhões).
                        numero_cartao  BIGINT       NOT NULL,
                        nome           VARCHAR(120) NOT NULL,
    -- true = ativo. O DEFAULT faz todo cartão novo já nascer ativo.
                        status         BOOLEAN      NOT NULL DEFAULT TRUE,
    -- Guarda o NOME do enum TipoCartao (6.2), em texto, e não a posição dele.
    -- Assim, reordenar o enum no Java não corrompe os dados.
                        tipo_cartao    VARCHAR(20)  NOT NULL,
                        usuario_id     BIGINT       NOT NULL,

                        CONSTRAINT uk_cartao_numero UNIQUE (numero_cartao),
                        CONSTRAINT ck_cartao_numero_positivo CHECK (numero_cartao > 0),
    -- Mesmo que alguém grave direto no banco, só os 3 tipos do desafio são aceitos.
                        CONSTRAINT ck_cartao_tipo CHECK (tipo_cartao IN ('COMUM', 'ESTUDANTE', 'TRABALHADOR')),
    -- Chave estrangeira SEM "ON DELETE CASCADE", de propósito: apagar os cartões é tarefa
    -- do módulo de cartão, via evento (7.3), dentro da mesma transação. Se ele falhar,
    -- o banco barra a exclusão do usuário, em vez de sumir com os cartões em silêncio.
                        CONSTRAINT fk_cartao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

-- O Postgres NÃO cria índice sozinho na chave estrangeira.
-- Este índice acelera "cartões do usuário X" e "remover cartões do usuário X" (6.3 e 7.3).
CREATE INDEX idx_cartao_usuario_id ON cartao (usuario_id);
