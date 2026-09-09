package com.shop.chatbot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatbotRequest(

        @NotBlank(message = "질문을 입력해주세요.")
        @Size(
                max = 1000,
                message = "질문은 1000자 이하로 입력해주세요."
        )
        String message,

        @Valid
        @Size(max = 6)
        List<ChatbotHistoryMessage> history,

        @Size(max = 100)
        @Pattern(
                regexp = "[a-zA-Z0-9_-]+",
                message = "올바르지 않은 대화 식별자입니다."
        )
        String conversationId

) {

    public ChatbotRequest {

        if (history == null) {
            history = List.of();
        }
    }
}