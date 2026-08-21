package com.shop.reservation.repository;

import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    /*
     * 해당 슬롯에 실제 예약된 인원 수를 계산합니다.
     *
     * CANCELED 예약은 점유 인원에서 제외합니다.
     */
    long countBySlot_IdAndStatus(
            Long slotId,
            ReservationStatus status
    );

    /*
     * 동일 회원이 같은 슬롯을 중복 예약하는 것을
     * Service 단계에서 확인할 때 사용합니다.
     */
    boolean existsByMember_IdAndSlot_IdAndStatus(
            Long memberId,
            Long slotId,
            ReservationStatus status
    );

    /*
     * ADMIN 예약 관리 화면에서
     * 최근 생성된 예약부터 조회합니다.
     */
    List<Reservation> findAllByOrderByIdDesc();
}