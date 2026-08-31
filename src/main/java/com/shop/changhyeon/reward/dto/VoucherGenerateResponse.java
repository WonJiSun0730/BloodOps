package com.shop.changhyeon.reward.dto;

public record VoucherGenerateResponse(
        Long productId,
        int generatedCount,
        long availableCount
) {
}
