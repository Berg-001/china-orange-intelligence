package com.omip.collector;
import org.junit.jupiter.api.Test; import static org.assertj.core.api.Assertions.assertThat;
class NantongBulletinParserTest {
 @Test void extractsOnlyOrangeRowsWithVerifiedUnit(){String html="""
  <html><head><title>2026年5月11日南通农副产品物流有限公司水果批发价格</title></head><body><table>
  <tr><th>商品</th><th>规格/等级</th><th>计量单位</th><th>批发价</th></tr>
  <tr><td>橙子</td><td>赣南</td><td>元/500克</td><td>2.5</td></tr>
  <tr><td>橙子</td><td>南非</td><td>元/500克</td><td>3.5</td></tr>
  <tr><td>苹果</td><td>山东</td><td>元/500克</td><td>5.5</td></tr></table></body></html>""";
  var rows=new NantongBulletinParser().parse(html,"https://fgw.nantong.gov.cn/example.html");
  assertThat(rows).hasSize(2); assertThat(rows.getFirst().price()).isEqualByComparingTo("2.5"); assertThat(rows.getFirst().unitWeightKg()).isEqualByComparingTo("0.5"); assertThat(rows.getFirst().referenceDate().toString()).isEqualTo("2026-05-11");
 }
}
