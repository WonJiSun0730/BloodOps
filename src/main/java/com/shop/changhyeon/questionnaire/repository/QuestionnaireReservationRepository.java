package com.shop.changhyeon.questionnaire.repository;

import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.entity.Reservation;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.Optional;

public interface QuestionnaireReservationRepository
        extends Repository<Reservation, Long> {

    Optional<Reservation> findFirstByMember_IdAndStatusAndSlot_ReservationDateGreaterThanEqualOrderBySlot_ReservationDateAscSlot_StartTimeAscIdAsc(
            Long memberId,
            ReservationStatus status,
            LocalDate reservationDate
    );

    Optional<Reservation> findFirstByMember_IdOrderByIdDesc(
            Long memberId
    );

    Optional<Reservation> findFirstByMember_IdAndSlot_Center_IdOrderByIdDesc(
            Long memberId,
            Long centerId
    );
}
