package com.shop.changhyeon.donation.dto;

import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.reservation.constant.DonationType;

import java.time.LocalDate;

public record DonationHistoryResponseDto(
        Long id,
        String donorName,
        LocalDate donationDate,
        DonationType donationType,
        String bloodCenterName,
        String donationPlaceName
) {
    public static DonationHistoryResponseDto from(DonationHistory history) {
        return new DonationHistoryResponseDto(
                history.getId(),
                history.getDonorName(),
                history.getDonationDate(),
                history.getDonationType(),
                history.getBloodCenterName(),
                history.getDonationPlaceName()
        );
    }
}
