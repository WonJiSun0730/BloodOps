package com.shop.changhyeon.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KakaoMessageTemplateUpdateRequest(
        boolean enabled,

        @NotBlank(message = "발송 내용을 입력해주세요.")
        @Size(
                max = 200,
                message = "카카오 기본 텍스트 템플릿은 200자까지 입력할 수 있습니다."
        )
        String messageText
) {
}
