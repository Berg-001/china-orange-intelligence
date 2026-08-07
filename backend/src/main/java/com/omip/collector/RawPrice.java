package com.omip.collector;

import com.omip.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RawPrice(String country, String city, String market, Product product, Category category,
                       BigDecimal price, String currency, String unit, BigDecimal unitWeightKg,
                       String source, String url, SourceType sourceType, LocalDate referenceDate,
                       String productNameOriginal, String specification) {
    public RawPrice(String country, String city, String market, Product product, Category category,
                    BigDecimal price, String currency, String unit, BigDecimal unitWeightKg,
                    String source, String url, SourceType sourceType, LocalDate referenceDate) {
        this(country, city, market, product, category, price, currency, unit, unitWeightKg,
                source, url, sourceType, referenceDate, product.name(), null);
    }
}
