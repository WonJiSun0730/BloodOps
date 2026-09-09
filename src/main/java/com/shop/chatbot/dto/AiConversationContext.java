package com.shop.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiConversationContext(

        @JsonProperty("last_intent")
        String lastIntent,

        @JsonProperty("last_keyword")
        String lastKeyword

) {
}