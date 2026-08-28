package com.shop.changhyeon.reward.dto;

public record RewardPurchaseResponse(
        Long purchaseId,
        String productName,
        int pointsSpent,
        long remainingBalance,
        int remainingStock
) {
}
