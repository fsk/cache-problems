package com.lab.distributed.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Pulls in shared beans when a level application scans {@code com.lab.distributed}
 * or imports this auto-configuration.
 */
@AutoConfiguration
@ComponentScan(basePackages = {
        "com.lab.distributed.cache",
        "com.lab.distributed.retry",
        "com.lab.distributed.exception"
})
public class LabAutoConfiguration {}
