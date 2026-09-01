package com.shop.changhyeon.promotion.dto;

import com.shop.changhyeon.promotion.constant.*;
import com.shop.changhyeon.promotion.entity.*;
import java.time.*;
import java.util.List;

public record PromotionResponse(
        Long id,
        PromotionCategory category,
        PromotionType type,
        String regionName,
        String regionDisplayName,
        String title,
        String summary,
        String content,
        String imageUrl,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate donationEligibilityStartDate,
        boolean active,
        boolean entered,
        boolean eligibleToEnter,
        String eligibilityMessage,
        List<OptionResponse> options,
        LocalDateTime createdAt
) {
    public record OptionResponse(Long id, String label) {}

    public static PromotionResponse from(Promotion promotion, boolean entered) {
        return from(promotion, entered, false, null);
    }

    public static PromotionResponse from(
            Promotion promotion,
            boolean entered,
            boolean eligibleToEnter,
            String eligibilityMessage
    ) {
        PromotionRegion region = promotion.getRegionName() == null
                ? null
                : PromotionRegion.find(promotion.getRegionName()).orElse(null);
        String regionName = region == null
                ? promotion.getRegionName()
                : region.name();
        String regionDisplayName = region == null
                ? promotion.getRegionName()
                : region.displayName();
        String imageUrl = promotion.getImageData() != null && promotion.getImageData().length > 0
                ? "/api/promotions/" + promotion.getId() + "/image"
                : promotion.getImageUrl();

        return new PromotionResponse(
                promotion.getId(),
                promotion.getCategory(),
                promotion.getType(),
                regionName,
                regionDisplayName,
                promotion.getTitle(),
                promotion.getSummary(),
                promotion.getContent(),
                imageUrl,
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getDonationEligibilityStartDate(),
                promotion.isActive(),
                entered,
                eligibleToEnter,
                eligibilityMessage,
                promotion.getOptions().stream()
                        .map(option -> new OptionResponse(option.getId(), option.getLabel()))
                        .toList(),
                promotion.getCreatedAt()
        );
    }
}
