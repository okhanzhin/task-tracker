package com.okhanzhin.task.tracker.backend.scheduler;

import com.okhanzhin.task.tracker.backend.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/**
 *
 * @author okhanzhin on 10.11.2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevokedTokenCleanupScheduler {

    private final RevokedTokenRepository revokedTokenRepository;
    private final Clock clock;

    @Scheduled(
            cron = "${app.security.revoked-tokens.cleanup-cron:0 0 * * * *}",
            zone = "UTC"
    )
    public void deleteExpiredTokens() {
        int deleted =
                revokedTokenRepository.deleteExpired(
                        Instant.now(clock)
                );

        log.debug("Deleted {} expired token revocations", deleted);
    }
}
