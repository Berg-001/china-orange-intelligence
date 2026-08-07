ALTER TABLE market_price ADD COLUMN product_name_original VARCHAR(255);
ALTER TABLE market_price ADD COLUMN specification VARCHAR(500);
UPDATE market_price SET product_name_original = product WHERE product_name_original IS NULL;
ALTER TABLE market_price ALTER COLUMN product_name_original SET NOT NULL;
