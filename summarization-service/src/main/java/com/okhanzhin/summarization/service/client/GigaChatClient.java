package com.okhanzhin.summarization.service.client;

import com.okhanzhin.summarization.service.config.properties.GigaChatProperties;
import com.okhanzhin.summarization.service.exception.GigaChatClientException;
import com.okhanzhin.summarization.service.model.GigaChatChatRequest;
import com.okhanzhin.summarization.service.model.GigaChatChatResponse;
import com.okhanzhin.summarization.service.model.GigaChatMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
@Component
public class GigaChatClient {

    private final RestClient restClient;
    private final GigaChatTokenProvider tokenProvider;
    private final GigaChatProperties properties;

    public GigaChatClient(
            @Qualifier("gigaChatApiRestClient") RestClient restClient,
            GigaChatTokenProvider tokenProvider,
            GigaChatProperties properties
    ) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
        this.properties = properties;
    }

    public String generate(String systemPrompt, String userPrompt) {
        Assert.hasText(systemPrompt, "System prompt must not be blank");
        Assert.hasText(userPrompt, "User prompt must not be blank");

        GigaChatChatRequest request = new GigaChatChatRequest(
                properties.getModel(),
                List.of(
                        new GigaChatMessage("system", systemPrompt),
                        new GigaChatMessage("user", userPrompt)
                ),
                false
        );

        String accessToken = tokenProvider.getAccessToken();

        GigaChatChatResponse response;

        try {
            response = restClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .body(request)
                    .retrieve()
                    .body(GigaChatChatResponse.class);
        } catch (RestClientResponseException exception) {
            throw new GigaChatClientException(
                    "GigaChat generation request failed with HTTP status "
                            + exception.getStatusCode().value(),
                    exception
            );
        } catch (RestClientException exception) {
            throw new GigaChatClientException(
                    "Failed to execute GigaChat generation request",
                    exception
            );
        }

        return extractText(response);
    }

    private String extractText(GigaChatChatResponse response) {
        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()) {
            throw new GigaChatClientException(
                    "GigaChat response does not contain any choices"
            );
        }

        GigaChatChatResponse.Choice choice =
                response.getChoices().getFirst();

        if (choice == null) {
            throw new GigaChatClientException(
                    "GigaChat response contains an empty choice"
            );
        }

        if (!"stop".equals(choice.getFinishReason())) {
            throw new GigaChatClientException(
                    "GigaChat generation did not finish normally: finishReason="
                            + choice.getFinishReason()
            );
        }

        GigaChatMessage message = choice.getMessage();

        if (message == null || !StringUtils.hasText(message.getContent())) {
            throw new GigaChatClientException(
                    "GigaChat response does not contain generated text"
            );
        }

        return message.getContent().strip();
    }
}
