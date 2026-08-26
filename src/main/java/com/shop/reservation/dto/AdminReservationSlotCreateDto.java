package com.shop.reservation.dto;

import com.shop.reservation.constant.DonationType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AdminReservationSlotCreateDto {

    private Long centerId;

    private LocalDate reservationDate;

    private DonationType donationType;

    private LocalTime startTime;

    private LocalTime endTime;

    private int intervalMinutes;
}