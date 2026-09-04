package com.shop.donationtip.dto;

import com.shop.donationtip.constant.DonationTipCategory;

import java.util.List;

public record DonationTipRequest(
        DonationTipCategory category,
        String title,
        List<DonationTipItemRequest> items
) {
}