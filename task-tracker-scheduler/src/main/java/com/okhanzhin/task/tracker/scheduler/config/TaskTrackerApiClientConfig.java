package com.okhanzhin.task.tracker.scheduler.config;

import com.okhanzhin.task.tracker.scheduler.config.properties.TaskTrackerApiClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static com.okhanzhin.task.tracker.scheduler.utils.Constants.BEARER_PREFIX;

/**
 *
 * @author okhanzhin on 05.10.2026
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@EnableConfigurationProperties(TaskTrackerApiClientProperties.class)
public class TaskTrackerApiClientConfig {

    @Bean
    ClientHttpRequestFactory trackerRequestFactory(TaskTrackerApiClientProperties properties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(properties.getConnectTimeout())
                .withReadTimeout(properties.getReadTimeout());

        return ClientHttpRequestFactoryBuilder.jdk().build(settings);
    }

    @Bean
    RestClient trackerBackendRestClient(
            RestClient.Builder builder,
            ClientHttpRequestFactory trackerRequestFactory,
            TaskTrackerApiClientProperties properties
    ) {
        return builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + properties.getServiceToken())
                .requestFactory(trackerRequestFactory)
                .build();
    }
}
