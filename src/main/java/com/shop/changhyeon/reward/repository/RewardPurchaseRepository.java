package com.shop.changhyeon.reward.repository;

import com.shop.changhyeon.reward.entity.RewardPurchase;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RewardPurchaseRepository extends JpaRepository<RewardPurchase, Long> {
    List<RewardPurchase> findByMember_IdOrderByCreatedAtDescIdDesc(Long memberId);
    Optional<RewardPurchase> findByIdAndMember_Id(Long id, Long memberId);

    @Query("""
        select purchase
        from RewardPurchase purchase
        left join fetch purchase.pickupCenter center
        where (:centerId is null or center.id = :centerId)
        order by purchase.createdAt desc, purchase.id desc
        """)
    Page<RewardPurchase> findAdminOrders(
            @Param("centerId") Long centerId,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select purchase from RewardPurchase purchase where purchase.id = :purchaseId")
    Optional<RewardPurchase> findByIdForUpdate(@Param("purchaseId") Long purchaseId);
}
