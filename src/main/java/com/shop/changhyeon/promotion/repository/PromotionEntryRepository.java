package com.shop.changhyeon.promotion.repository;
import com.shop.changhyeon.promotion.entity.PromotionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PromotionEntryRepository extends JpaRepository<PromotionEntry, Long> {
    boolean existsByPromotion_IdAndMember_Id(Long promotionId, Long memberId);
    long countByPromotion_Id(Long promotionId);
}
