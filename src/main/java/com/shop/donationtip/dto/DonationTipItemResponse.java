package com.shop.donationtip.dto;

import com.shop.donationtip.entity.DonationTipItem;

public record DonationTipItemResponse(
        Long id,
        Integer itemOrder,
        String title,
        String content,
        String imagePath
) {

    public static DonationTipItemResponse from(
            DonationTipItem item
    ) {
        return new DonationTipItemResponse(
                item.getId(),
                item.getItemOrder(),
                item.getTitle(),
                item.getContent(),
                item.getImagePath()
        );
    }
}