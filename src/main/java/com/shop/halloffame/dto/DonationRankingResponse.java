package com.shop.halloffame.dto;

import java.util.List;

public record DonationRankingResponse(
        String category,
        String categoryLabel,
        List<DonationRankingItemResponse> rankings
) {
}