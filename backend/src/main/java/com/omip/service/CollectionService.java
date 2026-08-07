package com.omip.service;

import com.omip.collector.PriceCollector;
import com.omip.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Slf4j @Service
public class CollectionService {
    private final List<PriceCollector> collectors; private final PriceNormalizer normalizer; private final ConsensusService consensus; private final MarketPriceRepository repository;
    public CollectionService(List<PriceCollector> collectors, PriceNormalizer normalizer, ConsensusService consensus, MarketPriceRepository repository) { this.collectors=collectors; this.normalizer=normalizer; this.consensus=consensus; this.repository=repository; }
    @Scheduled(cron="${omip.scheduler.cron:0 0 7 * * *}", zone="UTC") @Transactional
    public Map<String,Object> collectDaily() {
        var saved = collectors.stream().filter(PriceCollector::enabled).flatMap(c -> { try { return c.collect().stream(); } catch (RuntimeException e) { log.error("collector_failed collector={}", c.name(), e); return java.util.stream.Stream.empty(); } }).map(normalizer::normalize).map(repository::save).toList();
        var groups = saved.stream().filter(p -> p.getConfidenceScore() > 0).collect(Collectors.groupingBy(p ->
                new ConsensusKey(p.getReferenceDate(),p.getCountry(),p.getCity(),p.getMarket(),p.getProduct(),p.getCategory(),p.getSpecification())));
        var eligible=groups.values().stream().map(this::oneObservationPerSource).filter(v->v.size()>=2).toList();
        eligible.stream().map(consensus::calculate).forEach(repository::save);
        log.info("collection_completed records={} consensus={}", saved.size(), eligible.size());
        return Map.of("records", saved.size(), "collectors", collectors.size());
    }
    private List<MarketPrice> oneObservationPerSource(List<MarketPrice> values){return new ArrayList<>(values.stream().collect(Collectors.toMap(MarketPrice::getSource,v->v,(a,b)->a.getConfidenceScore()>=b.getConfidenceScore()?a:b)).values());}
    private record ConsensusKey(LocalDate date,String country,String city,String market,Product product,Category category,String specification){}
}
