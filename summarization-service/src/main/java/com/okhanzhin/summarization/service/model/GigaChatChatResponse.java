package com.okhanzhin.summarization.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GigaChatChatResponse {

    private List<Choice> choices;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {

        private GigaChatMessage message;

        @JsonProperty("finish_reason")
        private String finishReason;
    }
}
