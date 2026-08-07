package com.omip.collector;
import com.omip.domain.*; import org.jsoup.Jsoup; import org.springframework.stereotype.Component;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.*; import java.util.regex.Pattern;

@Component public class XinhuaOrangeJuiceParser {
 private static final Pattern DATE=Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");
 private static final Pattern FCOJ=Pattern.compile("国内冷冻浓缩橙汁出厂价(?:为|报)?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*元[/／]吨");
 private static final Pattern NFC=Pattern.compile("国内非浓缩还原橙汁出厂价(?:为|报)?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*元[/／]吨");
 public List<RawPrice> parse(String html,String url){var doc=Jsoup.parse(html,url);var text=doc.title()+" "+doc.text();var dm=DATE.matcher(text);if(!dm.find())throw new IllegalArgumentException("Xinhua article has no full reference date");var date=LocalDate.of(Integer.parseInt(dm.group(1)),Integer.parseInt(dm.group(2)),Integer.parseInt(dm.group(3)));List<RawPrice> out=new ArrayList<>();add(out,FCOJ,text,Product.FCOJ,"国内冷冻浓缩橙汁",date,url);add(out,NFC,text,Product.NFC,"国内非浓缩还原橙汁",date,url);if(out.isEmpty())throw new IllegalArgumentException("Xinhua article has no supported domestic factory-gate price");return out;}
 private void add(List<RawPrice> out,Pattern pattern,String text,Product product,String original,LocalDate date,String url){var m=pattern.matcher(text);if(m.find())out.add(new RawPrice("CN",null,"China national factory-gate aggregate",product,Category.INDUSTRY,new BigDecimal(m.group(1)),"CNY","tonne",null,"Xinhua Index Research Institute / Chongqing Three Gorges Citrus Trading Center",url,SourceType.SPECIALIZED,date,original,"Domestic factory-gate aggregate"));}
}
