package com.shop.changhyeon.promotion.dto;

import com.shop.changhyeon.promotion.constant.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record PromotionRequest(
        @NotNull(message = "프로모션 구분을 선택해주세요.") PromotionCategory category,
        @NotNull(message = "게시물 유형을 선택해주세요.") PromotionType type,
        @Size(max = 50, message = "지역 구분은 50자 이하로 입력해주세요.") String regionName,
        @NotBlank(message = "제목을 입력해주세요.") @Size(max = 150, message = "제목은 150자 이하로 입력해주세요.") String title,
        @Size(max = 300, message = "요약은 300자 이하로 입력해주세요.") String summary,
        String content,
        @NotNull(message = "게시 시작일을 선택해주세요.") LocalDate startDate,
        @NotNull(message = "게시 종료일을 선택해주세요.") LocalDate endDate,
        LocalDate donationEligibilityStartDate,
        boolean active,
        List<@NotBlank(message = "추첨 선택 항목을 입력해주세요.") @Size(max = 120, message = "추첨 선택 항목은 120자 이하로 입력해주세요.") String> options
) {}
