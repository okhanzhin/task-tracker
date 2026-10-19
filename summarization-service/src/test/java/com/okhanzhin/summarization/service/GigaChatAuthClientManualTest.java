package com.okhanzhin.summarization.service;

import com.okhanzhin.summarization.service.client.GigaChatAuthClient;
import com.okhanzhin.summarization.service.config.GigaChatConfig;
import com.okhanzhin.summarization.service.config.properties.GigaChatProperties;
import com.okhanzhin.summarization.service.model.GigaChatTokenResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
@SpringBootTest(
        classes = GigaChatAuthClientManualTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@EnabledIfEnvironmentVariable(
        named = "GIGACHAT_MANUAL_TEST_ENABLED",
        matches = "true"
)
public class GigaChatAuthClientManualTest {

    @Autowired
    private GigaChatAuthClient authClient;

    @Test
    void shouldRequestAccessToken() {
        GigaChatTokenResponse response = authClient.requestToken();

        assertThat(response.getAccessToken() != null
                && !response.getAccessToken().isBlank()).isTrue();

        assertThat(response.getExpiresAt())
                .isNotNull()
                .isGreaterThan(System.currentTimeMillis());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(GigaChatProperties.class)
    @Import({
            GigaChatConfig.class,
            GigaChatAuthClient.class
    })
    static class TestConfig {

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }
    }
}
