ALTER TABLE market_price ADD COLUMN fx_source VARCHAR(255) NOT NULL DEFAULT 'LEGACY_CONFIGURED_FALLBACK';
ALTER TABLE market_price ADD COLUMN fx_reference_date DATE;
UPDATE market_price SET fx_reference_date = reference_date WHERE fx_reference_date IS NULL;
ALTER TABLE market_price ALTER COLUMN fx_reference_date SET NOT NULL;
ALTER TABLE market_price ALTER COLUMN fx_source DROP DEFAULT;
