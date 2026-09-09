package com.shop.reservation.repository;

import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
     * 해당 슬롯을 참조하는 예약 이력이
     * 하나라도 존재하는지 확인합니다.
     *
     * RESERVED / CANCELED 모두 포함합니다.
     */
    boolean existsBySlot_Id(
            Long slotId
    );

    boolean existsBySlot_Center_Id(
            Long centerId
    );

    @Query("""
        select r
        from Reservation r
        join r.slot s
        where s.equipmentGroup.id = :equipmentGroupId
          and s.reservationDate = :reservationDate
          and r.status = :status
        """)
    List<Reservation> findEquipmentGroupReservations(
            @Param("equipmentGroupId")
            Long equipmentGroupId,

            @Param("reservationDate")
            LocalDate reservationDate,

            @Param("status")
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

    List<Reservation> findByMember_IdAndStatusOrderByIdDesc(
            Long memberId,
            ReservationStatus status
    );

    List<Reservation> findByMember_EmailOrderByIdDesc(
            String email
    );

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

    @Query("""
        select r
        from Reservation r
        join fetch r.member m
        join fetch r.slot s
        join fetch s.center c
        where s.reservationDate = :reservationDate
          and (:centerId is null or c.id = :centerId)
        order by s.startTime asc, r.id asc
        """)
    List<Reservation> findMedicalTodayReservations(
            @Param("reservationDate") LocalDate reservationDate,
            @Param("centerId") Long centerId
    );
}
