package com.shop.changhyeon.donation.dto;

import java.time.LocalDate;

public record DonationEligibilityResponseDto(

        LocalDate wholeBloodNextAvailableDate,
        LocalDate plateletNextAvailableDate,
        LocalDate plasmaNextAvailableDate,
        LocalDate plateletPlasmaNextAvailableDate,

        String wholeBloodRestrictionReason,
        String plateletRestrictionReason,
        String plasmaRestrictionReason,
        String plateletPlasmaRestrictionReason,

        int wholeBloodCountLastYear,
        int wholeBloodAnnualLimit,

        int plateletCountLastYear,
        int plateletAnnualLimit

) {
}