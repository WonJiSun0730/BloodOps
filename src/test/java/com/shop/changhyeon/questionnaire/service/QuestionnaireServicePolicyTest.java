package com.shop.changhyeon.questionnaire.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuestionnaireServicePolicyTest {

    @Test
    void allowsDonorOnExactSixteenthBirthday() {
        assertDoesNotThrow(() -> QuestionnaireService.validateDonationAge(
                LocalDate.of(2010, 9, 1),
                LocalDate.of(2026, 9, 1)
        ));
    }

    @Test
    void rejectsDonorOneDayBeforeSixteenthBirthday() {
        assertThrows(IllegalArgumentException.class, () ->
                QuestionnaireService.validateDonationAge(
                        LocalDate.of(2010, 9, 2),
                        LocalDate.of(2026, 9, 1)
                ));
    }
}
