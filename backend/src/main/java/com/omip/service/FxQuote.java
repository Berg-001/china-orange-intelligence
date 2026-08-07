package com.omip.service;
import java.math.BigDecimal; import java.time.LocalDate;
public record FxQuote(BigDecimal rate, LocalDate referenceDate, String source) {}
