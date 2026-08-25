package com.shop.reservation.repository;

import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    boolean existsByMember_IdAndSlot_ReservationDateAndStatus(
            Long memberId,
            java.time.LocalDate reservationDate,
            ReservationStatus status
    );

    @Query("""
        select r
        from Reservation r
        join r.member m
        join r.slot s
        join s.center c
        where (
            :keyword is null
            or :keyword = ''
            or lower(m.name)
                like lower(concat('%', :keyword, '%'))
            or lower(c.centerName)
                like lower(concat('%', :keyword, '%'))
        )
        and (
            :status is null
            or r.status = :status
        )
        order by r.id desc
        """)
    Page<Reservation> findAdminReservations(
            @Param("keyword") String keyword,
            @Param("status") ReservationStatus status,
            Pageable pageable
    );

    List<Reservation> findByMember_EmailOrderByIdDesc(
            String email
    );
}