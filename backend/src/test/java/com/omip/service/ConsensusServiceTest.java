package com.omip.service;
import com.omip.domain.*; import org.junit.jupiter.api.Test; import java.math.BigDecimal; import java.time.*; import java.util.List; import static org.assertj.core.api.Assertions.*;
class ConsensusServiceTest {
 private MarketPrice value(String usd,int score,String source){return MarketPrice.builder().country("CN").city("Beijing").market("m").product(Product.FRESH_ORANGE).productNameOriginal("orange").category(Category.WHOLESALE).price(new BigDecimal(usd)).currency("USD").unit("kg").pricePerKg(new BigDecimal(usd)).priceUsd(new BigDecimal(usd)).priceBrl(new BigDecimal(usd)).priceCny(new BigDecimal(usd)).fxSource("TEST").fxReferenceDate(LocalDate.now()).source(source).url("u").sourceType(SourceType.OFFICIAL).confidenceScore(score).collectionTime(Instant.now()).referenceDate(LocalDate.now()).build();}
 @Test void calculatesConfidenceWeightedMean(){var result=new ConsensusService().calculate(List.of(value("10",100,"a"),value("20",50,"b")));assertThat(result.getPriceUsd()).isEqualByComparingTo("13.333333");assertThat(result.isConsensus()).isTrue();}
 @Test void rejectsMultipleRowsFromSamePublisher(){assertThatThrownBy(()->new ConsensusService().calculate(List.of(value("10",100,"a"),value("20",100,"a")))).hasMessageContaining("distinct sources");}
 @Test void rejectsDifferentMarkets(){var other=value("20",100,"b");other.setMarket("another");assertThatThrownBy(()->new ConsensusService().calculate(List.of(value("10",100,"a"),other))).hasMessageContaining("not comparable");}
}
