package com.shop.chatbot.dto;

import java.util.List;

public record ChatbotResponse(
        String message,
        String intent,
        List<ChatbotAction> actions,
        List<String> sources
) {

    public static ChatbotResponse text(
            String message
    ) {
        return new ChatbotResponse(
                message,
                "GENERAL",
                List.of(),
                List.of()
        );
    }
}