package com.omip.service;

import com.omip.collector.RawPrice;
import com.omip.domain.MarketPrice;
import org.springframework.stereotype.Service;
import java.math.*;
import java.time.Instant;

@Service
public class PriceNormalizer {
    private static final BigDecimal DEFAULT_BOX_KG = new BigDecimal("40.8");
    private final ExchangeRateService fx;
    public PriceNormalizer(ExchangeRateService fx) { this.fx = fx; }
    public MarketPrice normalize(RawPrice raw) {
        if (!"CN".equalsIgnoreCase(raw.country()))
            throw new IllegalArgumentException("OMIP accepts only prices observed in the Chinese market (country=CN)");
        BigDecimal kg = switch(raw.unit().toLowerCase()) {
            case "kg", "kilogram" -> raw.price();
            case "box", "caixa" -> raw.price().divide(raw.unitWeightKg() == null ? DEFAULT_BOX_KG : raw.unitWeightKg(), 6, RoundingMode.HALF_UP);
            case "tonne", "metric_ton", "t" -> raw.price().divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);
            default -> { if (raw.unitWeightKg() == null) throw new IllegalArgumentException("Unit requires weight: " + raw.unit()); yield raw.price().divide(raw.unitWeightKg(), 6, RoundingMode.HALF_UP); }
        };
        var usdQuote=fx.quote(raw.currency(), "USD", raw.referenceDate());
        var brlQuote=fx.quote(raw.currency(), "BRL", raw.referenceDate());
        var cnyQuote=fx.quote(raw.currency(), "CNY", raw.referenceDate());
        return MarketPrice.builder().country(raw.country()).city(raw.city()).market(raw.market()).product(raw.product())
                .productNameOriginal(raw.productNameOriginal()).specification(raw.specification()).category(raw.category())
                .price(raw.price()).currency(raw.currency().toUpperCase()).unit(raw.unit()).pricePerKg(kg)
                .priceUsd(convert(kg,usdQuote)).priceBrl(convert(kg,brlQuote)).priceCny(convert(kg,cnyQuote))
                .fxSource(usdQuote.source()).fxReferenceDate(usdQuote.referenceDate())
                .source(raw.source()).url(raw.url()).sourceType(raw.sourceType()).confidenceScore(raw.sourceType().defaultScore())
                .collectionTime(Instant.now()).referenceDate(raw.referenceDate()).consensus(false).build();
    }
    private BigDecimal convert(BigDecimal value, FxQuote quote) { return value.multiply(quote.rate()).setScale(6, RoundingMode.HALF_UP); }
}
