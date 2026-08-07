package com.omip.service;
import com.omip.collector.RawPrice; import com.omip.domain.*; import org.junit.jupiter.api.Test; import java.math.BigDecimal; import java.time.LocalDate; import static org.assertj.core.api.Assertions.assertThat;
class PriceNormalizerTest {
 private final ExchangeRateService fx=(from,to,date)->new FxQuote(BigDecimal.ONE,date,"TEST");
 @Test void convertsStandardBoxToKilograms(){var raw=new RawPrice("CN","Beijing","Market",Product.FRESH_ORANGE,Category.WHOLESALE,new BigDecimal("160"),"CNY","box",null,"source","https://example.com",SourceType.RECOGNIZED_MARKET,LocalDate.of(2026,8,6)); var result=new PriceNormalizer(fx).normalize(raw); assertThat(result.getPricePerKg()).isEqualByComparingTo("3.921569"); assertThat(result.getPrice()).isEqualByComparingTo("160");}
 @Test void preservesKilogramPrice(){var raw=new RawPrice("CN",null,"Market",Product.INDUSTRIAL_ORANGE,Category.INDUSTRY,new BigDecimal("2.50"),"CNY","kg",null,"source","https://example.com",SourceType.OFFICIAL,LocalDate.now()); assertThat(new PriceNormalizer(fx).normalize(raw).getPricePerKg()).isEqualByComparingTo("2.50");}
 @Test void rejectsPricesOutsideChina(){var raw=new RawPrice("BR",null,"Market",Product.FRESH_ORANGE,Category.WHOLESALE,BigDecimal.ONE,"BRL","kg",null,"source","https://example.com",SourceType.OFFICIAL,LocalDate.now()); org.assertj.core.api.Assertions.assertThatThrownBy(()->new PriceNormalizer(fx).normalize(raw)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Chinese market");}
}
