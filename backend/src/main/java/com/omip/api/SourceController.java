package com.omip.api;
import com.omip.domain.MarketPriceRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController public class SourceController {
 private final MarketPriceRepository repository; public SourceController(MarketPriceRepository r){repository=r;}
 @GetMapping("/sources") public List<Map<String,Object>> sources(){return repository.sources().stream().map(x->Map.<String,Object>of("name",x[0],"url",x[1],"type",x[2],"confidenceScore",x[3])).toList();}
}
