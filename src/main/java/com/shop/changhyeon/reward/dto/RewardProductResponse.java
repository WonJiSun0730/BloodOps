package com.shop.changhyeon.reward.dto;

import com.shop.changhyeon.reward.entity.RewardProduct;

public record RewardProductResponse(
        Long id,
        String name,
        String description,
        int pointPrice,
        int stockQuantity,
        boolean active
) {
    public static RewardProductResponse from(RewardProduct product) {
        return new RewardProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPointPrice(),
                product.getStockQuantity(),
                product.isActive()
        );
    }
}
