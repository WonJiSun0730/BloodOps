package com.shop.changhyeon.reward.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AdminTestPointRequest(
        @Min(1)
        @Max(1_000_000)
        int points
) {
}
