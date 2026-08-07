package com.omip.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Slf4j @Component
@ConditionalOnProperty(name="omip.collectors.nantong.enabled", havingValue="true")
public class NantongBulletinCollector implements PriceCollector {
    private final List<String> urls; private final NantongBulletinParser parser;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NORMAL).build();
    public NantongBulletinCollector(@Value("${omip.collectors.nantong.urls:}") String urls, NantongBulletinParser parser) {
        this.urls = Arrays.stream(urls.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList(); this.parser = parser;
    }
    public String name(){return "nantong-government-bulletins";} public boolean enabled(){return !urls.isEmpty();}
    public List<RawPrice> collect(){
        List<RawPrice> result=new ArrayList<>();
        for(String url:urls){try{
            var request=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30)).header("User-Agent","OMIP/0.1 market-research contact=repository").GET().build();
            var response=client.send(request,HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if(response.statusCode()!=200) throw new IllegalStateException("HTTP "+response.statusCode());
            result.addAll(parser.parse(response.body(),url));
        }catch(Exception e){log.error("nantong_bulletin_failed url={}",url,e);}}
        return result;
    }
}
