package com.shop.changhyeon.management.repository;

import com.shop.changhyeon.management.dto.StockSummaryDto;
import com.shop.changhyeon.management.entity.BloodUnit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

public interface BloodUnitRepository extends JpaRepository<BloodUnit, Long>, JpaSpecificationExecutor<BloodUnit> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BloodUnit b WHERE b.productType=:productType AND b.bloodType=:bloodType AND b.status='보유중' AND b.expirationDate>CURRENT_TIMESTAMP ORDER BY b.expirationDate")
    List<BloodUnit> findAvailableStockForUpdate(@Param("productType") String productType, @Param("bloodType") String bloodType);
    Optional<BloodUnit> findByUnitId(String unitId);
    List<BloodUnit> findAllByCenterIsNull();
    boolean existsByUnitIdAndIdNot(String unitId, Long id);
    long countByStatusIn(Collection<String> statuses);
    long countByStatusInAndExpirationDateBetween(Collection<String> statuses, LocalDateTime from, LocalDateTime to);
    boolean existsByUnitIdStartingWith(String prefix);
    @Transactional @Modifying(clearAutomatically = true)
    @Query("DELETE FROM BloodUnit b WHERE b.unitId LIKE CONCAT(:prefix, '%')")
    int deleteByUnitIdPrefix(@Param("prefix") String prefix);
    @Query("SELECT new com.shop.changhyeon.management.dto.StockSummaryDto(b.productType,b.bloodType,CAST(COUNT(b) AS int)) FROM BloodUnit b WHERE b.status=:status AND b.expirationDate>CURRENT_TIMESTAMP GROUP BY b.productType,b.bloodType ORDER BY b.productType,b.bloodType")
    List<StockSummaryDto> findStockSummaryByStatus(@Param("status") String status);
}
