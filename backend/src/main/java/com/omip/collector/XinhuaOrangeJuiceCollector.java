package com.omip.collector;
import lombok.extern.slf4j.Slf4j; import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.stereotype.Component;
import java.net.URI; import java.net.http.*; import java.nio.charset.StandardCharsets; import java.time.Duration; import java.util.*;

@Slf4j @Component @ConditionalOnProperty(name="omip.collectors.xinhua.enabled",havingValue="true")
public class XinhuaOrangeJuiceCollector implements PriceCollector {
 private final List<String> urls; private final XinhuaOrangeJuiceParser parser; private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NORMAL).build();
 public XinhuaOrangeJuiceCollector(@Value("${omip.collectors.xinhua.urls:}")String urls,XinhuaOrangeJuiceParser parser){this.urls=Arrays.stream(urls.split(",")).map(String::trim).filter(s->!s.isBlank()).toList();this.parser=parser;}
 public String name(){return "xinhua-domestic-orange-juice";} public boolean enabled(){return !urls.isEmpty();}
 public List<RawPrice> collect(){List<RawPrice> out=new ArrayList<>();for(String url:urls)try{var req=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30)).header("User-Agent","OMIP/0.1 market-research contact=repository").GET().build();var res=client.send(req,HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));if(res.statusCode()!=200)throw new IllegalStateException("HTTP "+res.statusCode());out.addAll(parser.parse(res.body(),url));}catch(Exception e){log.error("xinhua_orange_juice_failed url={}",url,e);}return out;}
}
