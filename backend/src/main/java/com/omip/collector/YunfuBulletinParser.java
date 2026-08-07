package com.omip.collector;
import com.omip.domain.*; import org.jsoup.Jsoup; import org.springframework.stereotype.Component;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.*; import java.util.regex.Pattern;

@Component public class YunfuBulletinParser {
 private static final Pattern DATE=Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");
 public List<RawPrice> parse(String html,String url){
  var doc=Jsoup.parse(html,url); var matcher=DATE.matcher(doc.title()+" "+doc.select("h1,h2,h3").text());
  if(!matcher.find()) throw new IllegalArgumentException("Yunfu bulletin has no reference date");
  var date=LocalDate.of(Integer.parseInt(matcher.group(1)),Integer.parseInt(matcher.group(2)),Integer.parseInt(matcher.group(3)));
  List<RawPrice> out=new ArrayList<>();
  for(var row:doc.select("tr")){
   var cells=row.select("th,td").stream().map(org.jsoup.nodes.Element::text).toList();
   if(cells.size()<5||!"橙子".equals(cells.get(0).trim())) continue;
   var unit=cells.get(3).replace(" ",""); if(!unit.contains("元/500克")) continue;
   try{var price=new BigDecimal(cells.get(4).trim());var spec=cells.get(2).isBlank()?"Unspecified":cells.get(2).trim();
    out.add(new RawPrice("CN","Yunfu","Yunfu market basket monitoring points",Product.FRESH_ORANGE,Category.RETAIL,price,"CNY","500g",new BigDecimal("0.5"),"Yunfu Development and Reform Bureau",url,SourceType.OFFICIAL,date,"橙子",spec));
   }catch(NumberFormatException ignored){}
  }
  if(out.isEmpty()) throw new IllegalArgumentException("Yunfu bulletin contains no supported orange row"); return out;
 }
}
