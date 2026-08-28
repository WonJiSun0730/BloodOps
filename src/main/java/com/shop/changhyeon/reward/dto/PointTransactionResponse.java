package com.shop.changhyeon.reward.dto;

import com.shop.changhyeon.reward.entity.PointTransaction;
import com.shop.reservation.constant.DonationType;

import java.time.LocalDateTime;

public record PointTransactionResponse(
        Long id,
        int points,
        String description,
        DonationType donationType,
        LocalDateTime createdAt
) {
    public static PointTransactionResponse from(PointTransaction transaction) {
        return new PointTransactionResponse(
                transaction.getId(),
                transaction.getPoints(),
                transaction.getDescription(),
                transaction.getDonationType(),
                transaction.getCreatedAt()
        );
    }
}
