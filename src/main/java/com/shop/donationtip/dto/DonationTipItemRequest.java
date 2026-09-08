package com.shop.donationtip.dto;

public record DonationTipItemRequest(
        Integer itemOrder,
        String title,
        String content,
        String imagePath
) {
}