package com.lab.level1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "level1")
public record Level1Properties(Cache cache, Db db) {

    public record Cache(String keyPrefix, int ttlSeconds) {}

    public record Db(int simulatedLatencyMs) {}
}
