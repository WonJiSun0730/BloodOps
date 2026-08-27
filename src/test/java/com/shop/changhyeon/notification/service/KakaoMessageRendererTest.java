package com.shop.changhyeon.notification.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KakaoMessageRendererTest {

    private final KakaoMessageRenderer renderer =
            new KakaoMessageRenderer();

    @Test
    void previewReplacesAllReservationPlaceholders() {
        String preview = renderer.preview(
                KakaoMessageRenderer.DEFAULT_MESSAGE
        );

        assertTrue(preview.contains("홍길동"));
        assertTrue(preview.contains("2026-09-13 16:30"));
        assertTrue(preview.contains("혈소판"));
        assertTrue(preview.contains("헌혈의집 구월센터"));
        assertEquals(-1, preview.indexOf("#{"));
    }

    @Test
    void defaultMessageFitsKakaoTextTemplateLimit() {
        String preview = renderer.preview(
                KakaoMessageRenderer.DEFAULT_MESSAGE
        );

        renderer.validateLength(preview);
        assertTrue(renderer.length(preview) <= 200);
    }

    @Test
    void overTwoHundredCharactersAreRejected() {
        String tooLong = "가".repeat(201);

        assertThrows(
                IllegalArgumentException.class,
                () -> renderer.validateLength(tooLong)
        );
    }
}
