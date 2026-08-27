package com.shop.changhyeon.notification.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoMessageApiClient {

    private static final String SEND_URL =
            "https://kapi.kakao.com/v2/api/talk/memo/default/send";

    private final ObjectMapper objectMapper;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public void send(
            String accessToken,
            String messageText
    ) {
        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add(
                "template_object",
                templateObject(messageText)
        );

        JsonNode response = RestClient.create()
                .post()
                .uri(SEND_URL)
                .contentType(
                        MediaType.APPLICATION_FORM_URLENCODED
                )
                .headers(headers ->
                        headers.setBearerAuth(accessToken)
                )
                .body(form)
                .retrieve()
                .body(JsonNode.class);

        if (
                response == null
                        || response.path("result_code")
                        .asInt(-1) != 0
        ) {
            throw new IllegalStateException(
                    "카카오 메시지 API가 성공 결과를 반환하지 않았습니다."
            );
        }
    }

    private String templateObject(String messageText) {
        String linkUrl =
                frontendUrl.endsWith("/")
                        ? frontendUrl
                        : frontendUrl + "/";

        Map<String, Object> template = Map.of(
                "object_type", "text",
                "text", messageText,
                "link", Map.of(
                        "web_url", linkUrl,
                        "mobile_web_url", linkUrl
                )
        );

        try {
            return objectMapper.writeValueAsString(template);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "카카오 메시지 요청을 생성하지 못했습니다.",
                    exception
            );
        }
    }
}
