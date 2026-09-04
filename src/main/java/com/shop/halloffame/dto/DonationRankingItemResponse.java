package com.shop.halloffame.dto;

public record DonationRankingItemResponse(
        int rank,
        Long memberId,
        String name,
        long count
) {
}