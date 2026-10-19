-- Execute após 001-create-clientes.sql, no banco definido em DB_URL.
CREATE TABLE produtos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    preco DECIMAL(12, 2) NOT NULL,
    estoque BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_produtos_preco CHECK (preco > 0),
    CONSTRAINT chk_produtos_estoque CHECK (estoque IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
