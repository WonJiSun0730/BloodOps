package com.shop.changhyeon.qna.dto;

import com.shop.changhyeon.qna.entity.Qna;

import java.time.LocalDateTime;

public record QnaResponse(
        Long id,
        String title,
        String content,
        String authorName,
        boolean answered,
        String answer,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime answeredAt,
        boolean mine
) {
    public static QnaResponse from(Qna qna, Long viewerId) {
        return new QnaResponse(
                qna.getId(),
                qna.getTitle(),
                qna.getContent(),
                maskName(qna.getMember().getName()),
                qna.isAnswered(),
                qna.getAnswer(),
                qna.getCreatedAt(),
                qna.getUpdatedAt(),
                qna.getAnsweredAt(),
                viewerId != null && viewerId.equals(qna.getMember().getId())
        );
    }

    private static String maskName(String name) {
        if (name == null || name.isBlank()) return "회원";
        if (name.length() == 1) return name;
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }
}
