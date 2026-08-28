package com.shop.changhyeon.donation.dto;

import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.reservation.constant.DonationType;

import java.time.LocalDate;

public record DonationHistoryResponseDto(
        Long id,
        LocalDate donationDate,
        DonationType donationType,
        Long centerId,
        String bloodCenterName,
        String donationPlaceName
) {
    public static DonationHistoryResponseDto from(DonationHistory history) {
        return new DonationHistoryResponseDto(
                history.getId(),
                history.getDonationDate(),
                history.getDonationType(),
                history.getReservation()
                        .getSlot()
                        .getCenter()
                        .getId(),
                history.getBloodCenterName(),
                history.getDonationPlaceName()
        );
    }
}
