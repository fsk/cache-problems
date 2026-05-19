package com.lab.level2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Level2CacheInvalidationApplication {

    public static void main(String[] args) {
        SpringApplication.run(Level2CacheInvalidationApplication.class, args);
    }
}
