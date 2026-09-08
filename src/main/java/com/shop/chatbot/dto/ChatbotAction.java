package com.shop.chatbot.dto;

public record ChatbotAction(
        String type,
        String label,
        String url
) {
}