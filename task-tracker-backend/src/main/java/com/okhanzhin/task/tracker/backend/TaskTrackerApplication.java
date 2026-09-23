package com.okhanzhin.task.tracker.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 *
 * @author okhanzhin on 23.08.2026
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TaskTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskTrackerApplication.class, args);
    }
}
