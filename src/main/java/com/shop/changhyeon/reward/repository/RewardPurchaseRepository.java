package com.shop.changhyeon.reward.repository;

import com.shop.changhyeon.reward.entity.RewardPurchase;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface RewardPurchaseRepository extends JpaRepository<RewardPurchase, Long> {
    List<RewardPurchase> findByMember_IdOrderByCreatedAtDescIdDesc(Long memberId);
    List<RewardPurchase> findAllByOrderByCreatedAtDescIdDesc();
    Optional<RewardPurchase> findByIdAndMember_Id(Long id, Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select purchase from RewardPurchase purchase where purchase.id = :purchaseId")
    Optional<RewardPurchase> findByIdForUpdate(@Param("purchaseId") Long purchaseId);
}
