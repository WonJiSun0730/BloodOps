package com.shop.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
public record AiAnalyzeResponse(
        String intent,
        Double confidence,
        String keyword,

        @JsonProperty("knowledge_id")
        String knowledgeId,

        String answer,
        List<String> sources,

        @JsonProperty("matched_by")
        String matchedBy
) {

    public AiAnalyzeResponse {

        if (sources == null) {
            sources = List.of();
        }
    }
}