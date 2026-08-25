package com.shop.changhyeon.management.repository;

import com.shop.changhyeon.management.dto.StockSummaryDto;
import com.shop.changhyeon.management.entity.BloodUnit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BloodUnitRepository
        extends JpaRepository<BloodUnit, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b
        FROM BloodUnit b
        WHERE b.productType = :productType
          AND b.bloodType = :bloodType
          AND b.status = '보유중'
          AND b.expirationDate > CURRENT_TIMESTAMP
        ORDER BY b.expirationDate ASC
    """)
    List<BloodUnit> findAvailableStockForUpdate(
            @Param("productType") String productType,
            @Param("bloodType") String bloodType
    );

    Optional<BloodUnit> findByUnitId(String unitId);

    /**
     * 오늘 생성된 더미 데이터 존재 확인
     *
     * 예:
     * DUMMY-BLD-20260825-
     */
    boolean existsByUnitIdStartingWith(String prefix);

    /**
     * 자동 생성 더미 데이터 삭제
     *
     * 실제 혈액:
     * BLD-...
     *
     * 자동 더미:
     * DUMMY-BLD-...
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
        DELETE
        FROM BloodUnit b
        WHERE b.unitId LIKE CONCAT(:prefix, '%')
    """)
    int deleteByUnitIdPrefix(
            @Param("prefix") String prefix
    );

    @Query("""
        SELECT new com.shop.changhyeon.management.dto.StockSummaryDto(
            b.productType,
            b.bloodType,
            CAST(COUNT(b) AS int)
        )
        FROM BloodUnit b
        WHERE b.status = :status
          AND b.expirationDate > CURRENT_TIMESTAMP
        GROUP BY
            b.productType,
            b.bloodType
        ORDER BY
            b.productType,
            b.bloodType
    """)
    List<StockSummaryDto> findStockSummaryByStatus(
            @Param("status") String status
    );
}