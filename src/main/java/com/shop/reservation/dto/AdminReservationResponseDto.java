package com.shop.reservation.dto;

import com.shop.reservation.constant.DonationType;
import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.entity.Reservation;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class AdminReservationResponseDto {

    private final Long id;

    private final Long memberId;

    private final String memberName;

    private final String memberEmail;

    private final Long centerId;

    private final String centerName;

    private final Long slotId;

    private final LocalDate reservationDate;

    private final LocalTime startTime;

    private final LocalTime endTime;

    private final DonationType donationType;

    private final ReservationStatus status;


    public AdminReservationResponseDto(
            Reservation reservation
    ) {
        this.id = reservation.getId();

        this.memberId =
                reservation.getMember().getId();

        this.memberName =
                reservation.getMember().getName();

        this.memberEmail =
                reservation.getMember().getEmail();

        this.slotId =
                reservation.getSlot().getId();

        this.centerId =
                reservation.getSlot()
                        .getCenter()
                        .getId();

        this.centerName =
                reservation.getSlot()
                        .getCenter()
                        .getCenterName();

        this.reservationDate =
                reservation.getSlot()
                        .getReservationDate();

        this.startTime =
                reservation.getSlot()
                        .getStartTime();

        this.endTime =
                reservation.getSlot()
                        .getEndTime();

        this.donationType =
                reservation.getSlot()
                        .getDonationType();

        this.status =
                reservation.getStatus();
    }
}