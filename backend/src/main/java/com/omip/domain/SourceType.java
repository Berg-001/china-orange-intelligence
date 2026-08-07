package com.omip.domain;
public enum SourceType {
    OFFICIAL(100), GOVERNMENT_API(95), RECOGNIZED_MARKET(90), SPECIALIZED(80), SCRAPING(60), BLOG(40), DERIVED(0), SAMPLE(0);
    private final int defaultScore;
    SourceType(int defaultScore) { this.defaultScore = defaultScore; }
    public int defaultScore() { return defaultScore; }
}
