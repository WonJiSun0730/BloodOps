package com.shop.changhyeon.promotion.dto;

import com.shop.changhyeon.promotion.constant.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record PromotionRequest(
        @NotNull PromotionCategory category,
        @NotNull PromotionType type,
        @Size(max = 50) String regionName,
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 300) String summary,
        @NotBlank String content,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        LocalDate donationEligibilityStartDate,
        boolean active,
        List<@NotBlank @Size(max = 120) String> options
) {}
