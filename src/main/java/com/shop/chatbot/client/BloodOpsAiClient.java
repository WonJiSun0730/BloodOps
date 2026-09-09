package com.shop.chatbot.client;

import com.shop.chatbot.dto.AiAnalyzeRequest;
import com.shop.chatbot.dto.AiAnalyzeResponse;
import com.shop.chatbot.dto.AiConversationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class BloodOpsAiClient {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BloodOpsAiClient.class
            );

    private final RestClient restClient;

    public BloodOpsAiClient(
            RestClient.Builder restClientBuilder,
            @Value("${ai.service.url}")
            String aiServiceUrl
    ) {
        this.restClient =
                restClientBuilder
                        .baseUrl(aiServiceUrl)
                        .build();
    }

    public Optional<AiAnalyzeResponse> analyze(
            String message,
            String lastIntent,
            String lastKeyword
    ) {

        AiAnalyzeRequest request =
                new AiAnalyzeRequest(
                        message,
                        new AiConversationContext(
                                lastIntent,
                                lastKeyword
                        )
                );

        try {

            AiAnalyzeResponse response =
                    restClient
                            .post()
                            .uri("/analyze")
                            .body(request)
                            .retrieve()
                            .body(
                                    AiAnalyzeResponse.class
                            );

            return Optional.ofNullable(
                    response
            );

        } catch (RestClientException exception) {

            /*
             * Python AI 장애가 Spring 전체 장애로
             * 전파되지 않도록 빈 결과로 반환합니다.
             */
            log.warn(
                    "BloodOps AI 서비스 호출 실패: {}",
                    exception.getMessage()
            );

            return Optional.empty();
        }
    }
}