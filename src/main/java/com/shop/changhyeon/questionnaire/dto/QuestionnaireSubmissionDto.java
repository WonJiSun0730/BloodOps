package com.shop.changhyeon.questionnaire.dto;

import com.shop.changhyeon.questionnaire.constant.QuestionnaireStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record QuestionnaireSubmissionDto(
        Long id,
        String receiptCode,
        String donorName,
        String maskedResidentRegistrationNumber,
        QuestionnaireStatus status,
        LocalDateTime submittedAt,
        LocalDateTime expiresAt,
        Double weightKg,
        Map<String, String> answers,
        Map<String, String> answerDetails,
        List<String> reviewReasons,
        boolean identityVerified,
        String notice
) {
}
