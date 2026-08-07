package com.omip.collector;
import org.junit.jupiter.api.Test; import static org.assertj.core.api.Assertions.assertThat;
class XinhuaOrangeJuiceParserTest {
 @Test void extractsOnlyDomesticFactoryGatePrices(){String html="""
  <html><head><title>新华指数|2026年07月09日</title></head><body>
  全球冷冻浓缩橙汁期货合约结算均价为171.25美分/磅；国内冷冻浓缩橙汁出厂价为25411.85元/吨；
  国内非浓缩还原橙汁出厂价为7193.19元/吨。
  </body></html>
  """;var rows=new XinhuaOrangeJuiceParser().parse(html,"https://indices.cnfin.com/example.html");assertThat(rows).hasSize(2);assertThat(rows.get(0).price().toPlainString()).isEqualTo("25411.85");assertThat(rows.get(1).price().toPlainString()).isEqualTo("7193.19");assertThat(rows).allMatch(r->r.category().name().equals("INDUSTRY"));}
}
