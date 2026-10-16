package com.okhanzhin.task.tracker.scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
@Configuration(proxyBeanMethods = false)
public class TimeConfig {

    @Bean
    public Clock reportClock() {
        return Clock.systemUTC();
    }
}
