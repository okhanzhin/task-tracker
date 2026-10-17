package com.okhanzhin.summarization.service.client;


import com.okhanzhin.summarization.service.config.properties.GigaChatProperties;
import com.okhanzhin.summarization.service.model.GigaChatTokenResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
/**
 *
 * @author okhanzhin on 17.10.2026
 */
@Component
public class GigaChatTokenProvider {

    private final GigaChatAuthClient authClient;
    private final Clock clock;
    private final Duration refreshMargin;

    private String accessToken;
    private Instant expiresAt;

    public GigaChatTokenProvider(
            GigaChatAuthClient authClient,
            @Qualifier("gigaChatClock") Clock clock,
            GigaChatProperties properties
    ) {
        this.authClient = authClient;
        this.clock = clock;
        this.refreshMargin = properties.getTokenRefreshMargin();
    }

    public synchronized String getAccessToken() {
        if (shouldRefreshToken()) {
            refreshToken();
        }

        return accessToken;
    }

    private boolean shouldRefreshToken() {
        if (accessToken == null || expiresAt == null) {
            return true;
        }

        Instant refreshAt = expiresAt.minus(refreshMargin);

        return !clock.instant().isBefore(refreshAt);
    }

    private void refreshToken() {
        GigaChatTokenResponse response = authClient.requestToken();

        if (response == null
                || !StringUtils.hasText(response.getAccessToken())
                || response.getExpiresAt() == null) {
            throw new IllegalStateException(
                    "GigaChat token response is invalid"
            );
        }

        Instant newExpiresAt =
                Instant.ofEpochMilli(response.getExpiresAt());

        if (!newExpiresAt.isAfter(clock.instant().plus(refreshMargin))) {
            throw new IllegalStateException(
                    "GigaChat token lifetime is shorter than the refresh margin"
            );
        }

        accessToken = response.getAccessToken();
        expiresAt = newExpiresAt;
    }
}
