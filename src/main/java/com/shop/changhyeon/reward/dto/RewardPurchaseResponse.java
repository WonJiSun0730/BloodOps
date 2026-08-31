package com.shop.changhyeon.reward.dto;

import com.shop.changhyeon.reward.constant.RewardProductType;
import com.shop.changhyeon.reward.constant.RewardPurchaseStatus;

public record RewardPurchaseResponse(
        Long purchaseId,
        String productName,
        RewardProductType productType,
        RewardPurchaseStatus status,
        int pointsSpent,
        long remainingBalance,
        int remainingStock,
        String pickupCode
) {
}
