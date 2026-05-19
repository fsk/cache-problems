package com.lab.level1;

import com.lab.level1.config.Level1Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(Level1Properties.class)
public class Level1CacheAsideApplication {

    public static void main(String[] args) {
        SpringApplication.run(Level1CacheAsideApplication.class, args);
    }
}
