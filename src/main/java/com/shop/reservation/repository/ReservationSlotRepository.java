package com.shop.reservation.repository;

import com.shop.reservation.constant.DonationType;
import com.shop.reservation.entity.ReservationSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationSlotRepository
        extends JpaRepository<ReservationSlot, Long> {

    boolean existsByCenter_IdAndReservationDateAndStartTimeAndDonationType(
            Long centerId,
            LocalDate reservationDate,
            LocalTime startTime,
            DonationType donationType
    );

    List<ReservationSlot>
    findByCenter_IdAndReservationDateAndDonationTypeOrderByStartTimeAsc(
            Long centerId,
            LocalDate reservationDate,
            DonationType donationType
    );

    List<ReservationSlot>
    findByCenter_IdAndReservationDateBetweenOrderByReservationDateAscStartTimeAsc(
            Long centerId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<ReservationSlot>
    findByCenter_IdAndReservationDateBetweenAndActiveTrueOrderByReservationDateAscStartTimeAsc(
            Long centerId,
            LocalDate startDate,
            LocalDate endDate
    );

    void deleteByCenter_Id(
            Long centerId
    );
}