package com.shop.reservation.dto;

import com.shop.reservation.constant.DonationType;
import com.shop.reservation.entity.ReservationSlot;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class AdminReservationSlotResponseDto {

    private final Long id;

    private final Long centerId;

    private final String centerName;

    private final LocalDate reservationDate;

    private final LocalTime startTime;

    private final LocalTime endTime;

    private final DonationType donationType;

    private final int capacity;

    private final long reservedCount;

    private final long remainingCapacity;

    private final boolean hasReservationHistory;

    private final boolean active;


    public AdminReservationSlotResponseDto(
            ReservationSlot slot,
            int equipmentCapacity,
            long reservedCount,
            long remainingCapacity,
            boolean hasReservationHistory
    ) {
        this.id = slot.getId();

        this.centerId =
                slot.getCenter().getId();

        this.centerName =
                slot.getCenter().getCenterName();

        this.reservationDate =
                slot.getReservationDate();

        this.startTime =
                slot.getStartTime();

        this.endTime =
                slot.getEndTime();

        this.donationType =
                slot.getDonationType();

        this.capacity =
                equipmentCapacity;

        this.reservedCount =
                reservedCount;

        this.remainingCapacity =
                Math.max(
                        remainingCapacity,
                        0
                );

        this.hasReservationHistory =
                hasReservationHistory;

        this.active =
                slot.isActive();
    }
}