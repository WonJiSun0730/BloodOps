package com.shop.halloffame.dto;

public record MyDonationSummaryResponse(
        String memberName,
        long totalCount,
        long wholeBloodCount,
        long plasmaCount,
        long plateletCount,
        long plateletPlasmaCount
) {
}