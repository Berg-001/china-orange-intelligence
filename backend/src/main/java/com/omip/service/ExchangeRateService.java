package com.omip.service;
import java.math.BigDecimal;
import java.time.LocalDate;
public interface ExchangeRateService { FxQuote quote(String from, String to, LocalDate date); }
