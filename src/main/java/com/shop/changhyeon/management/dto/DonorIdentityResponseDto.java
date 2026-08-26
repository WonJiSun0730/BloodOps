package com.shop.changhyeon.management.dto;

import com.shop.changhyeon.questionnaire.dto.QuestionnaireSubmissionDto;

public record DonorIdentityResponseDto(
        Long confirmationId,
        boolean existingDonor,
        String maskedResidentRegistrationNumber,
        DonorConfirmationResponseDto donor,
        QuestionnaireSubmissionDto questionnaire
) {}
