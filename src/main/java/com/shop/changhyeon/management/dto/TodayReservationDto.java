package com.shop.changhyeon.management.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record TodayReservationDto(
        Long id,
        Long memberId,
        Long slotId,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        String donationType,
        String status
) {}