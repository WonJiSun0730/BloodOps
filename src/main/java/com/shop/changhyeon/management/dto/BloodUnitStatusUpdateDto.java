package com.shop.changhyeon.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BloodUnitStatusUpdateDto(
        @NotBlank(message = "상태를 선택해 주세요.")
        @Pattern(
                regexp = "보유중|예약|보류|폐기",
                message = "올바른 상태를 선택해 주세요."
        )
        String status
) {}