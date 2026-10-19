-- Execute após 001-create-clientes.sql e 002-create-produtos.sql.
CREATE TABLE pedidos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_pedidos_quantidade CHECK (quantidade > 0),
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_pedidos_produto FOREIGN KEY (produto_id) REFERENCES produtos (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
