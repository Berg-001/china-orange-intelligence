package com.omip.collector;
import org.junit.jupiter.api.Test; import static org.assertj.core.api.Assertions.assertThat;
class YunfuBulletinParserTest {
 @Test void extractsOfficialRetailOrange(){String html="""
  <html><head><title>云浮市菜篮子价格监测（2026年7月14日）</title></head><body><table>
  <tr><th>品种</th><th>规格</th><th>产地</th><th>单位</th><th>价格</th></tr>
  <tr><td>橙子</td><td></td><td>国产</td><td>元/500克</td><td>4.50</td></tr>
  </table></body></html>
  """;
  var rows=new YunfuBulletinParser().parse(html,"https://www.yunfu.gov.cn/example.html");
  assertThat(rows).hasSize(1); assertThat(rows.getFirst().category().name()).isEqualTo("RETAIL");
  assertThat(rows.getFirst().price()).isEqualByComparingTo("4.50"); assertThat(rows.getFirst().specification()).isEqualTo("国产");
 }
}
