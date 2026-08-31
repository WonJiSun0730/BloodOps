package com.shop.changhyeon.reward.dto;

import com.shop.changhyeon.reward.constant.RewardProductType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RewardProductRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 500) String description,
        @Min(1) int pointPrice,
        @Min(0) int stockQuantity,
        RewardProductType productType,
        boolean active
) {
    public RewardProductType resolvedProductType() {
        return productType == null ? RewardProductType.PHYSICAL : productType;
    }
}
