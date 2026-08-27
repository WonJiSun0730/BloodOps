package com.shop.changhyeon.notification.dto;

public record KakaoMessageTemplateResponse(
        boolean enabled,
        String messageText,
        String previewText,
        int previewLength,
        int maximumLength
) {
}
