package com.omip.domain;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, UUID>, JpaSpecificationExecutor<MarketPrice> {
    List<MarketPrice> findByReferenceDateAndCountryAndProductAndCategoryAndConsensusFalse(LocalDate date, String country, Product product, Category category);
    @Query("select p from MarketPrice p where p.country='CN' and p.referenceDate = (select max(x.referenceDate) from MarketPrice x where x.country='CN') order by p.confidenceScore desc")
    List<MarketPrice> latest();
    @Query("select distinct p.source, p.url, p.sourceType, p.confidenceScore from MarketPrice p where p.country='CN' order by p.confidenceScore desc")
    List<Object[]> sources();
}
