package com.shop.changhyeon.reward.dto;

import com.shop.changhyeon.reward.constant.RewardProductType;
import com.shop.changhyeon.reward.constant.RewardPurchaseStatus;
import com.shop.changhyeon.reward.entity.RewardPurchase;
import com.shop.changhyeon.reward.entity.RewardVoucherCode;

import java.time.LocalDateTime;

public record RewardOrderResponse(
        Long id,
        String productName,
        RewardProductType productType,
        RewardPurchaseStatus status,
        int pointsSpent,
        LocalDateTime createdAt,
        String pickupCenterName,
        String pickupCode,
        LocalDateTime pickupDeadline,
        LocalDateTime pickedUpAt,
        String maskedVoucherCode,
        LocalDateTime voucherExpiresAt
) {
    public static RewardOrderResponse from(
            RewardPurchase purchase,
            RewardVoucherCode voucher
    ) {
        return new RewardOrderResponse(
                purchase.getId(),
                purchase.getProductName(),
                purchase.getProductType() == null ? RewardProductType.PHYSICAL : purchase.getProductType(),
                purchase.getStatus() == null ? RewardPurchaseStatus.COMPLETED : purchase.getStatus(),
                purchase.getPointsSpent(),
                purchase.getCreatedAt(),
                purchase.getPickupCenter() == null
                        ? null
                        : purchase.getPickupCenter().getCenterName(),
                purchase.getPickupCode(),
                purchase.getPickupDeadline(),
                purchase.getPickedUpAt(),
                voucher == null ? null : voucher.getMaskedCode(),
                voucher == null ? null : voucher.getExpiresAt()
        );
    }
}
