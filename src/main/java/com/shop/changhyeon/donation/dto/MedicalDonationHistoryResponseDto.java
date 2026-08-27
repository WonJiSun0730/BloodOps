package com.shop.changhyeon.donation.dto;

import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.reservation.constant.DonationType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MedicalDonationHistoryResponseDto(
        Long id,
        Long memberId,
        String donorName,
        LocalDateTime createdAt,
        LocalDate donationDate,
        DonationType donationType,
        String bloodCenterName,
        String donationPlaceName
) {
    public static MedicalDonationHistoryResponseDto from(DonationHistory history) {
        return new MedicalDonationHistoryResponseDto(
                history.getId(),
                history.getMember().getId(),
                history.getDonorName(),
                history.getRegTime(),
                history.getDonationDate(),
                history.getDonationType(),
                history.getBloodCenterName(),
                history.getDonationPlaceName()
        );
    }
}
