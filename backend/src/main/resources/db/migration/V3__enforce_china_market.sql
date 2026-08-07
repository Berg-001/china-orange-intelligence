-- NOT VALID preserves any legacy comparative rows while rejecting every new non-China record.
ALTER TABLE market_price ADD CONSTRAINT chk_market_price_china_only CHECK (country = 'CN') NOT VALID;
