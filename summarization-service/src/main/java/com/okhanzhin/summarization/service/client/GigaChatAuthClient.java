package com.okhanzhin.summarization.service.client;

import com.okhanzhin.summarization.service.config.properties.GigaChatProperties;
import com.okhanzhin.summarization.service.model.GigaChatTokenResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
@Component
public class GigaChatAuthClient {

    private final RestClient restClient;
    private final GigaChatProperties properties;

    public GigaChatAuthClient(
            @Qualifier("gigaChatAuthRestClient") RestClient restClient,
            GigaChatProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public GigaChatTokenResponse requestToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("scope", properties.getScope());

        GigaChatTokenResponse response = restClient.post()
                .uri("/api/v2/oauth")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("RqUID", UUID.randomUUID().toString())
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Basic " + properties.getAuthorizationKey()
                )
                .body(form)
                .retrieve()
                .body(GigaChatTokenResponse.class);

        validateResponse(response);

        return response;
    }

    private void validateResponse(GigaChatTokenResponse response) {
        if (response == null) {
            throw new IllegalStateException(
                    "GigaChat token response body is empty"
            );
        }

        if (!StringUtils.hasText(response.getAccessToken())) {
            throw new IllegalStateException(
                    "GigaChat token response does not contain an access token"
            );
        }

        if (response.getExpiresAt() == null || response.getExpiresAt() <= 0) {
            throw new IllegalStateException(
                    "GigaChat token response contains an invalid expiration time"
            );
        }
    }
}
