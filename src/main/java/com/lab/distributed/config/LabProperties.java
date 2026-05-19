package com.lab.distributed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lab")
public record LabProperties(
        String levelName,
        Redis redis,
        Postgres postgres,
        Cache cache
) {
    public record Redis(String host, int port, int database) {}
    public record Postgres(String url, String username, String password) {}
    public record Cache(String keyPrefix, int defaultTtlSeconds) {}
}
