package com.okhanzhin.summarization.service.config;

import com.okhanzhin.summarization.service.config.properties.GigaChatProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Clock;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
@Configuration(proxyBeanMethods = false)
public class GigaChatConfig {

    @Bean
    public RestClient gigaChatAuthRestClient(
            RestClient.Builder builder,
            GigaChatProperties properties
    ) {
        return builder
                .baseUrl(properties.getAuthBaseUrl().toString())
                .requestFactory(createRequestFactory(properties))
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    @Bean
    public RestClient gigaChatApiRestClient(
            RestClient.Builder builder,
            GigaChatProperties properties
    ) {
        return builder
                .baseUrl(properties.getApiBaseUrl().toString())
                .requestFactory(createRequestFactory(properties))
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    @Bean
    public Clock gigaChatClock() {
        return Clock.systemUTC();
    }

    private SimpleClientHttpRequestFactory createRequestFactory(
            GigaChatProperties properties
    ) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());

        return requestFactory;
    }
}
