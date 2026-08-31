package com.shop.changhyeon.reward.repository;

import com.shop.changhyeon.reward.constant.VoucherCodeStatus;
import com.shop.changhyeon.reward.entity.RewardVoucherCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDateTime;
import java.util.Optional;

public interface RewardVoucherCodeRepository extends JpaRepository<RewardVoucherCode, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RewardVoucherCode> findFirstByProductIdAndStatusAndExpiresAtAfterOrderByIdAsc(
            Long productId, VoucherCodeStatus status, LocalDateTime now);
    Optional<RewardVoucherCode> findByPurchaseId(Long purchaseId);
    long countByProductIdAndStatus(Long productId, VoucherCodeStatus status);
    void deleteByProductIdAndStatus(Long productId, VoucherCodeStatus status);
}
