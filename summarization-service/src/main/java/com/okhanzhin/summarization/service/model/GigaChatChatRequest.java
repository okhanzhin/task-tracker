package com.okhanzhin.summarization.service.model;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class GigaChatChatRequest {

    private String model;
    private List<GigaChatMessage> messages;
    private boolean stream;
}
