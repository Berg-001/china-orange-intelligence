package com.omip.collector;
import java.util.List;
public interface PriceCollector { String name(); boolean enabled(); List<RawPrice> collect(); }
