package com.okhanzhin.summarization.service;

import com.okhanzhin.summarization.service.client.GigaChatAuthClient;
import com.okhanzhin.summarization.service.client.GigaChatTokenProvider;
import com.okhanzhin.summarization.service.config.properties.GigaChatProperties;
import com.okhanzhin.summarization.service.model.GigaChatTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
public class GigaChatTokenProviderTest {

    private static final Instant NOW =
            Instant.parse("2026-10-17T15:00:00Z");

    private GigaChatAuthClient authClient;
    private Clock clock;
    private GigaChatTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        authClient = mock(GigaChatAuthClient.class);
        clock = mock(Clock.class);

        GigaChatProperties properties = new GigaChatProperties();
        properties.setTokenRefreshMargin(Duration.ofMinutes(2));

        when(clock.instant()).thenReturn(NOW);

        tokenProvider = new GigaChatTokenProvider(
                authClient,
                clock,
                properties
        );
    }

    @Test
    void shouldRequestTokenOnFirstCall() {
        when(authClient.requestToken())
                .thenReturn(token("first-token", NOW.plusSeconds(1800)));

        String result = tokenProvider.getAccessToken();

        assertThat(result).isEqualTo("first-token");
        verify(authClient).requestToken();
    }

    @Test
    void shouldReuseCachedToken() {
        when(authClient.requestToken())
                .thenReturn(token("first-token", NOW.plusSeconds(1800)));

        tokenProvider.getAccessToken();

        when(clock.instant()).thenReturn(NOW.plusSeconds(60));

        String result = tokenProvider.getAccessToken();

        assertThat(result).isEqualTo("first-token");
        verify(authClient, times(1)).requestToken();
    }

    @Test
    void shouldRefreshTokenAtRefreshBoundary() {
        when(authClient.requestToken()).thenReturn(
                token("first-token", NOW.plusSeconds(1800)),
                token("second-token", NOW.plusSeconds(3480))
        );

        tokenProvider.getAccessToken();

        when(clock.instant()).thenReturn(NOW.plusSeconds(1680));

        String result = tokenProvider.getAccessToken();

        assertThat(result).isEqualTo("second-token");
        verify(authClient, times(2)).requestToken();
    }

    @Test
    void shouldRejectExpiredTokenAndRequestAgainOnNextCall() {
        when(authClient.requestToken()).thenReturn(
                token("expired-token", NOW.minusSeconds(1)),
                token("valid-token", NOW.plusSeconds(1800))
        );

        assertThatThrownBy(() -> tokenProvider.getAccessToken())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "GigaChat token lifetime is shorter than the refresh margin"
                );

        assertThat(tokenProvider.getAccessToken())
                .isEqualTo("valid-token");

        verify(authClient, times(2)).requestToken();
    }

    @Test
    void shouldPropagateAuthFailureAndRequestAgainOnNextCall() {
        when(authClient.requestToken())
                .thenThrow(new IllegalStateException("Auth request failed"))
                .thenReturn(token("valid-token", NOW.plusSeconds(1800)));

        assertThatThrownBy(() -> tokenProvider.getAccessToken())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Auth request failed");

        assertThat(tokenProvider.getAccessToken())
                .isEqualTo("valid-token");

        verify(authClient, times(2)).requestToken();
    }

    private GigaChatTokenResponse token(
            String value,
            Instant expiration
    ) {
        GigaChatTokenResponse response = new GigaChatTokenResponse();
        response.setAccessToken(value);
        response.setExpiresAt(expiration.toEpochMilli());

        return response;
    }
}
