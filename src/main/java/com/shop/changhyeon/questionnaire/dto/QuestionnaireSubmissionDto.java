package com.shop.changhyeon.questionnaire.dto;

import com.shop.changhyeon.questionnaire.constant.QuestionnaireStatus;
import com.shop.changhyeon.questionnaire.constant.QuestionnaireVisitType;
import com.shop.reservation.constant.ReservationStatus;

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
        String contactPhone,
        String contactEmail,
        QuestionnaireVisitType visitType,
        Long reservationId,
        Long donationCenterId,
        String donationCenterName,
        String donationCenterAddress,
        ReservationStatus reservationStatus,
        Map<String, String> answers,
        Map<String, String> answerDetails,
        List<String> reviewReasons,
        boolean identityVerified,
        String notice
) {
}
