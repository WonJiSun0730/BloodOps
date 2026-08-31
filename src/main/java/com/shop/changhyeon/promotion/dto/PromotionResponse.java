package com.shop.changhyeon.promotion.dto;
import com.shop.changhyeon.promotion.constant.*;
import com.shop.changhyeon.promotion.entity.*;
import java.time.*;
import java.util.List;
public record PromotionResponse(Long id, PromotionCategory category, PromotionType type, String regionName, String title, String summary, String content, String imageUrl, LocalDate startDate, LocalDate endDate, boolean active, boolean entered, List<OptionResponse> options, LocalDateTime createdAt) {
    public record OptionResponse(Long id, String label) {}
    public static PromotionResponse from(Promotion p, boolean entered) { return new PromotionResponse(p.getId(), p.getCategory(), p.getType(), p.getRegionName(), p.getTitle(), p.getSummary(), p.getContent(), p.getImageUrl(), p.getStartDate(), p.getEndDate(), p.isActive(), entered, p.getOptions().stream().map(o -> new OptionResponse(o.getId(), o.getLabel())).toList(), p.getCreatedAt()); }
}
