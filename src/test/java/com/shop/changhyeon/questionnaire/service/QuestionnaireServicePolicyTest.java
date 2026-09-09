package com.shop.changhyeon.questionnaire.service;

import com.shop.reservation.entity.Reservation;
import com.shop.reservation.entity.ReservationSlot;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    void acceptsReservationBeforeQuestionnaireExpires() {
        Reservation reservation = reservationAt(
                LocalDate.of(2026, 9, 12),
                LocalTime.of(9, 59)
        );

        org.junit.jupiter.api.Assertions.assertTrue(
                QuestionnaireService.isReservationWithinValidity(
                        reservation,
                        LocalDateTime.of(2026, 9, 9, 10, 0),
                        LocalDateTime.of(2026, 9, 12, 10, 0)
                )
        );
    }

    @Test
    void rejectsReservationAtExactQuestionnaireExpirationTime() {
        Reservation reservation = reservationAt(
                LocalDate.of(2026, 9, 12),
                LocalTime.of(10, 0)
        );

        org.junit.jupiter.api.Assertions.assertFalse(
                QuestionnaireService.isReservationWithinValidity(
                        reservation,
                        LocalDateTime.of(2026, 9, 9, 10, 0),
                        LocalDateTime.of(2026, 9, 12, 10, 0)
                )
        );
    }

    @Test
    void rejectsReservationBeforeQuestionnaireSubmission() {
        Reservation reservation = reservationAt(
                LocalDate.of(2026, 9, 9),
                LocalTime.of(9, 59)
        );

        org.junit.jupiter.api.Assertions.assertFalse(
                QuestionnaireService.isReservationWithinValidity(
                        reservation,
                        LocalDateTime.of(2026, 9, 9, 10, 0),
                        LocalDateTime.of(2026, 9, 12, 10, 0)
                )
        );
    }

    @Test
    void rejectsDonorOneDayBeforeSixteenthBirthday() {
        assertThrows(IllegalArgumentException.class, () ->
                QuestionnaireService.validateDonationAge(
                        LocalDate.of(2010, 9, 2),
                        LocalDate.of(2026, 9, 1)
                ));
    }

    private Reservation reservationAt(
            LocalDate reservationDate,
            LocalTime startTime
    ) {
        ReservationSlot slot = ReservationSlot.builder()
                .reservationDate(reservationDate)
                .startTime(startTime)
                .build();
        return Reservation.builder()
                .slot(slot)
                .build();
    }
}
