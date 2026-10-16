package com.okhanzhin.task.tracker.backend;

import com.okhanzhin.task.tracker.backend.service.JwtService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

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

//    @Bean
//    CommandLineRunner printSchedulerToken(JwtService jwtService) {
//        return args -> System.out.println(
//                "SCHEDULER_SERVICE_TOKEN=" + jwtService.generateSchedulerToken()
//        );
//    }
}
