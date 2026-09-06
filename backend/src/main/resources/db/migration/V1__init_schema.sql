CREATE TABLE products (
    id              UUID PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    price           NUMERIC(12,2) NOT NULL,
    stock_quantity  INTEGER NOT NULL
);

CREATE TABLE orders (
    id               UUID PRIMARY KEY,
    customer_id      VARCHAR(255) NOT NULL,
    idempotency_key  VARCHAR(255) NOT NULL,
    status           VARCHAR(50) NOT NULL,
    total_amount     NUMERIC(12,2) NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now(),
    version          BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_orders_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);

CREATE TABLE order_items (
    id          UUID PRIMARY KEY,
    order_id    UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id  UUID NOT NULL,
    quantity    INTEGER NOT NULL,
    unit_price  NUMERIC(12,2) NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);

CREATE TABLE payments (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL,
    amount          NUMERIC(12,2) NOT NULL,
    status          VARCHAR(50) NOT NULL,
    failure_reason  VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
