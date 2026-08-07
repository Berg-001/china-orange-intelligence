package com.omip.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "market_price")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MarketPrice {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable=false, length=2) private String country;
    private String city;
    @Column(nullable=false) private String market;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Product product;
    @Column(name="product_name_original", nullable=false) private String productNameOriginal;
    private String specification;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Category category;
    @Column(nullable=false, precision=19, scale=6) private BigDecimal price;
    @Column(nullable=false, length=3) private String currency;
    @Column(name="price_brl", nullable=false, precision=19, scale=6) private BigDecimal priceBrl;
    @Column(name="price_usd", nullable=false, precision=19, scale=6) private BigDecimal priceUsd;
    @Column(name="price_cny", nullable=false, precision=19, scale=6) private BigDecimal priceCny;
    @Column(nullable=false) private String unit;
    @Column(name="price_per_kg", nullable=false, precision=19, scale=6) private BigDecimal pricePerKg;
    @Column(name="fx_source", nullable=false) private String fxSource;
    @Column(name="fx_reference_date", nullable=false) private LocalDate fxReferenceDate;
    @Column(nullable=false) private String source;
    @Column(nullable=false, length=2048) private String url;
    @Enumerated(EnumType.STRING) @Column(name="source_type", nullable=false) private SourceType sourceType;
    @Column(name="confidence_score", nullable=false) private Integer confidenceScore;
    @Column(name="collection_time", nullable=false) private Instant collectionTime;
    @Column(name="reference_date", nullable=false) private LocalDate referenceDate;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @Column(name="is_consensus", nullable=false) private boolean consensus;
    @PrePersist void create() { if (createdAt == null) createdAt = Instant.now(); }
}
