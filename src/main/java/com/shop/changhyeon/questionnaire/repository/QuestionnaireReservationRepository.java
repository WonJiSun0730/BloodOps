package com.shop.changhyeon.questionnaire.repository;

import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.entity.Reservation;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuestionnaireReservationRepository
        extends Repository<Reservation, Long> {

    Optional<Reservation> findById(Long reservationId);

    List<Reservation> findByDonorNameAndResidentNumberHashAndSlot_ReservationDateAndStatusAndSlot_Center_Id(
            String donorName,
            String residentNumberHash,
            LocalDate reservationDate,
            ReservationStatus status,
            Long centerId
    );

    Optional<Reservation> findFirstByMember_IdAndStatusAndSlot_ReservationDateBetweenOrderBySlot_ReservationDateAscSlot_StartTimeAscIdAsc(
            Long memberId,
            ReservationStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<Reservation> findFirstByMember_IdAndSlot_ReservationDateBetweenOrderBySlot_ReservationDateAscSlot_StartTimeAscIdAsc(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<Reservation> findFirstByMember_IdOrderByIdDesc(
            Long memberId
    );

    Optional<Reservation> findFirstByMember_IdAndSlot_Center_IdOrderByIdDesc(
            Long memberId,
            Long centerId
    );
}
