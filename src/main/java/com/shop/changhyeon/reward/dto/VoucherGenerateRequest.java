package com.shop.changhyeon.reward.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record VoucherGenerateRequest(
        @Min(1) @Max(100) int quantity,
        @Min(1) @Max(3650) int validDays
) {
}
