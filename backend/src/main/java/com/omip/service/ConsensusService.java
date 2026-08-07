package com.omip.service;

import com.omip.domain.*;
import org.springframework.stereotype.Service;
import java.math.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class ConsensusService {
    public MarketPrice calculate(List<MarketPrice> values) {
        if (values.size()<2 || values.stream().map(MarketPrice::getSource).distinct().count()<2)
            throw new IllegalArgumentException("Consensus requires at least two distinct sources");
        var first = values.getFirst();
        if(values.stream().anyMatch(v->!comparable(first,v)))
            throw new IllegalArgumentException("Consensus observations are not comparable");
        BigDecimal totalWeight = values.stream().map(v -> BigDecimal.valueOf(v.getConfidenceScore())).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.signum() == 0) totalWeight = BigDecimal.valueOf(values.size());
        final BigDecimal denominator = totalWeight;
        var usd = weighted(values, denominator, "USD");
        return MarketPrice.builder().country(first.getCountry()).city(first.getCity()).market("Daily weighted consensus")
                .product(first.getProduct()).productNameOriginal(first.getProductNameOriginal()).specification(first.getSpecification())
                .category(first.getCategory()).price(usd).currency("USD").unit("kg").pricePerKg(usd)
                .fxSource(first.getFxSource()).fxReferenceDate(first.getFxReferenceDate())
                .priceUsd(usd).priceBrl(weighted(values, denominator, "BRL")).priceCny(weighted(values, denominator, "CNY"))
                .source("OMIP consensus (" + values.size() + " sources)").url("https://github.com/Berg-001/china-orange-intelligence")
                .sourceType(SourceType.DERIVED).confidenceScore((int)Math.round(values.stream().mapToInt(MarketPrice::getConfidenceScore).average().orElse(0)))
                .collectionTime(Instant.now()).referenceDate(first.getReferenceDate()).consensus(true).build();
    }
    private BigDecimal weighted(List<MarketPrice> values, BigDecimal total, String currency) {
        return values.stream().map(v -> value(v, currency).multiply(BigDecimal.valueOf(v.getConfidenceScore() == 0 ? 1 : v.getConfidenceScore())))
                .reduce(BigDecimal.ZERO, BigDecimal::add).divide(total, 6, RoundingMode.HALF_UP);
    }
    private BigDecimal value(MarketPrice v, String currency) { return switch(currency) { case "BRL" -> v.getPriceBrl(); case "CNY" -> v.getPriceCny(); default -> v.getPriceUsd(); }; }
    private boolean comparable(MarketPrice a,MarketPrice b){return Objects.equals(a.getReferenceDate(),b.getReferenceDate())&&Objects.equals(a.getCountry(),b.getCountry())&&Objects.equals(a.getCity(),b.getCity())&&Objects.equals(a.getMarket(),b.getMarket())&&a.getProduct()==b.getProduct()&&a.getCategory()==b.getCategory()&&Objects.equals(a.getSpecification(),b.getSpecification());}
}
