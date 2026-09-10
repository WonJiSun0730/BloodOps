package com.shop.chatbot.dto;

public record AiAnalyzeRequest(
        String message,
        AiConversationContext context
) {
}