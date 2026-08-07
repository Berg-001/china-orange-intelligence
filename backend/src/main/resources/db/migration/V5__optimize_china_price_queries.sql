CREATE INDEX idx_market_price_china_latest ON market_price(reference_date DESC, confidence_score DESC) WHERE country='CN';
CREATE INDEX idx_market_price_consensus_inputs ON market_price(reference_date, city, market, product, category, specification, source) WHERE country='CN' AND is_consensus=FALSE;
