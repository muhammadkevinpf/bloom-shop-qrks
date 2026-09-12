CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE
    IF NOT EXISTS inventory (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        variant_id UUID NOT NULL UNIQUE,
        available_stock INT NOT NULL DEFAULT 0 CHECK (available_stock >= 0),
        reserved_stock INT NOT NULL DEFAULT 0 CHECK (reserved_stock >= 0),
        version BIGINT NOT NULL DEFAULT 0,
        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_inventory_variant_id ON inventory (variant_id);