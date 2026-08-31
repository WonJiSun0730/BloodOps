package com.shop.changhyeon.promotion.dto;
import java.time.LocalDateTime;
public record PromotionEntryResponse(Long entryId, Long promotionId, Long optionId, String optionLabel, LocalDateTime enteredAt) {}
