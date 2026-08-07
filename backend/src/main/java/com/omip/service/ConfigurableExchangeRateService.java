package com.omip.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.*;
import java.time.LocalDate;
import java.util.Map;

@Service
public class ConfigurableExchangeRateService implements ExchangeRateService {
    private final Map<String, BigDecimal> usdPerCurrency;
    public ConfigurableExchangeRateService(@Value("${omip.fx.usd-per-brl:0.18}") BigDecimal brl,
                                           @Value("${omip.fx.usd-per-cny:0.139}") BigDecimal cny) {
        usdPerCurrency = Map.of("USD", BigDecimal.ONE, "BRL", brl, "CNY", cny);
    }
    public FxQuote quote(String from, String to, LocalDate date) {
        var source = usdPerCurrency.get(from.toUpperCase()); var target = usdPerCurrency.get(to.toUpperCase());
        if (source == null || target == null) throw new IllegalArgumentException("Unsupported currency: " + from + " or " + to);
        return new FxQuote(source.divide(target, 12, RoundingMode.HALF_UP), date, "CONFIGURED_FALLBACK");
    }
}
