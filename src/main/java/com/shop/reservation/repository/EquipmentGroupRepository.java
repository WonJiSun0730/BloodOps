package com.shop.reservation.repository;

import com.shop.reservation.constant.EquipmentGroupType;
import com.shop.reservation.entity.EquipmentGroup;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentGroupRepository
        extends JpaRepository<EquipmentGroup, Long> {

    /*
     * 센터의 장비그룹 전체 조회.
     * 관리자 화면에서 장비 수량을 보여줄 때 사용합니다.
     */
    List<EquipmentGroup>
    findByCenter_IdOrderByGroupTypeAsc(
            Long centerId
    );


    /*
     * 일반 조회용.
     */
    Optional<EquipmentGroup>
    findByCenter_IdAndGroupType(
            Long centerId,
            EquipmentGroupType groupType
    );


    /*
     * 최종 예약 처리용.
     *
     * 같은 장비그룹에 동시에 예약이 들어올 때
     * 한 트랜잭션만 먼저 정원 검사를 진행하도록
     * DB row에 비관적 쓰기 잠금을 겁니다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select eg
            from EquipmentGroup eg
            where eg.center.id = :centerId
              and eg.groupType = :groupType
            """)
    Optional<EquipmentGroup>
    findForUpdate(
            @Param("centerId")
            Long centerId,

            @Param("groupType")
            EquipmentGroupType groupType
    );
}