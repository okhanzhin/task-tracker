package com.okhanzhin.task.tracker.backend.runner;

import com.okhanzhin.task.tracker.backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author okhanzhin on 10.11.2026
 */
@Component
@Profile("issue-service-token")
@RequiredArgsConstructor
public class ServiceTokenIssuerRunner implements CommandLineRunner {

    private final JwtService jwtService;

    @Override
    public void run(String... args) throws Exception {
        String token = jwtService.generateSchedulerToken();

        System.out.println("SCHEDULER_SERVICE_TOKEN=" + token);
    }
}
