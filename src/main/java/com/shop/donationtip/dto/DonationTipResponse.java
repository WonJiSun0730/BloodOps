package com.shop.donationtip.dto;

import com.shop.donationtip.constant.DonationTipCategory;
import com.shop.donationtip.entity.DonationTip;

import java.time.LocalDateTime;
import java.util.List;

public record DonationTipResponse(
        Long id,
        DonationTipCategory category,
        String categoryLabel,
        String title,
        String author,
        LocalDateTime regTime,
        LocalDateTime updateTime,
        List<DonationTipItemResponse> items
) {

    public static DonationTipResponse from(
            DonationTip tip
    ) {
        String author =
                tip.getMember() != null
                        ? tip.getMember().getName()
                        : "관리자";

        List<DonationTipItemResponse> items =
                tip.getItems()
                        .stream()
                        .map(
                                DonationTipItemResponse::from
                        )
                        .toList();

        return new DonationTipResponse(
                tip.getId(),
                tip.getCategory(),
                tip.getCategory().getLabel(),
                tip.getTitle(),
                author,
                tip.getRegTime(),
                tip.getUpdateTime(),
                items
        );
    }
}