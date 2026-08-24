package com.shop.changhyeon.management.repository;

import com.shop.changhyeon.management.dto.StockSummaryDto;
import com.shop.changhyeon.management.entity.BloodUnit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BloodUnitRepository extends JpaRepository<BloodUnit, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b FROM BloodUnit b
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

    boolean existsByDataSourceAndDummyGenerationDate(
            String dataSource,
            LocalDate dummyGenerationDate
    );

    @Modifying
    @Query("DELETE FROM BloodUnit b WHERE b.dataSource = :dataSource")
    int deleteByDataSource(@Param("dataSource") String dataSource);

    @Query("""
        SELECT new com.shop.changhyeon.management.dto.StockSummaryDto(b.productType, b.bloodType, CAST(COUNT(b) AS int))
        FROM BloodUnit b
        WHERE b.status = :status AND b.expirationDate > CURRENT_TIMESTAMP
        GROUP BY b.productType, b.bloodType
        ORDER BY b.productType, b.bloodType
    """)
    List<StockSummaryDto> findStockSummaryByStatus(@Param("status") String status);
}
