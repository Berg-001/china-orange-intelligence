package com.omip.collector;

import com.omip.domain.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@ConditionalOnProperty(name="omip.collectors.demo.enabled", havingValue="true")
public class DemoCollector implements PriceCollector {
    public String name() { return "demo"; }
    public boolean enabled() { return true; }
    public List<RawPrice> collect() {
        return List.of(new RawPrice("CN", "Beijing", "Demonstration Market", Product.FRESH_ORANGE,
                Category.WHOLESALE, new BigDecimal("160"), "CNY", "box", new BigDecimal("40.8"),
                "OMIP demonstration data", "https://example.invalid/demo", SourceType.SAMPLE, LocalDate.now()));
    }
}
