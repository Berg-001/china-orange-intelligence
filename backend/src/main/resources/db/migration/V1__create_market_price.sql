CREATE TABLE market_price (
 id UUID PRIMARY KEY, country VARCHAR(2) NOT NULL, city VARCHAR(255), market VARCHAR(255) NOT NULL,
 product VARCHAR(40) NOT NULL, category VARCHAR(40) NOT NULL, price NUMERIC(19,6) NOT NULL,
 currency VARCHAR(3) NOT NULL, price_brl NUMERIC(19,6) NOT NULL, price_usd NUMERIC(19,6) NOT NULL,
 price_cny NUMERIC(19,6) NOT NULL, unit VARCHAR(50) NOT NULL, price_per_kg NUMERIC(19,6) NOT NULL,
 source VARCHAR(255) NOT NULL, url VARCHAR(2048) NOT NULL, source_type VARCHAR(40) NOT NULL,
 confidence_score INTEGER NOT NULL CHECK(confidence_score BETWEEN 0 AND 100), collection_time TIMESTAMPTZ NOT NULL,
 reference_date DATE NOT NULL, created_at TIMESTAMPTZ NOT NULL, is_consensus BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_market_price_lookup ON market_price(country, product, category, reference_date DESC);
CREATE INDEX idx_market_price_source ON market_price(source, reference_date DESC);
