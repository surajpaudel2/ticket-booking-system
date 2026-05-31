package com.tbs.eventservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fixture.lock")
@Getter
@Setter
public class LockProperties {
    private int maxAttempts = 5;
    private long initialWaitMs = 50;
    private long maxWaitMs = 500;
}