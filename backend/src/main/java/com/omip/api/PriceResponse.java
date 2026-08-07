package com.omip.api;
import com.omip.domain.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
public record PriceResponse(UUID id, String country, String city, String market, Product product, String productNameOriginal, String specification, Category category,
 BigDecimal originalPrice, String originalCurrency, String originalUnit, BigDecimal pricePerKg,
 BigDecimal priceUsd, BigDecimal priceBrl, BigDecimal priceCny, String fxSource, LocalDate fxReferenceDate, String source, String url, int confidenceScore,
 LocalDate referenceDate, Instant collectionTime, boolean consensus) {
 public static PriceResponse from(MarketPrice p) { return new PriceResponse(p.getId(),p.getCountry(),p.getCity(),p.getMarket(),p.getProduct(),p.getProductNameOriginal(),p.getSpecification(),p.getCategory(),p.getPrice(),p.getCurrency(),p.getUnit(),p.getPricePerKg(),p.getPriceUsd(),p.getPriceBrl(),p.getPriceCny(),p.getFxSource(),p.getFxReferenceDate(),p.getSource(),p.getUrl(),p.getConfidenceScore(),p.getReferenceDate(),p.getCollectionTime(),p.isConsensus()); }
}
