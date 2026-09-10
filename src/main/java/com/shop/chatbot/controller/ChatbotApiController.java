package com.shop.chatbot.controller;

import com.shop.chatbot.dto.ChatbotRequest;
import com.shop.chatbot.dto.ChatbotResponse;
import com.shop.chatbot.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotApiController {

    private final ChatbotService chatbotService;

    public ChatbotApiController(
            ChatbotService chatbotService
    ) {
        this.chatbotService =
                chatbotService;
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatbotResponse> chat(
            @Valid
            @RequestBody
            ChatbotRequest request,
            Authentication authentication
    ) {

        String authenticationIdentifier =
                authentication != null
                        && authentication.isAuthenticated()
                        ? authentication.getName()
                        : null;

        ChatbotResponse response =
                chatbotService.chat(
                        request.message(),
                        request.history(),
                        authenticationIdentifier,
                        request.conversationId()
                );

        return ResponseEntity.ok(
                response
        );
    }
}