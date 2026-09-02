package com.shop.bloodhold.dto;

import java.time.LocalDate;

public record BloodHoldGuideResponse(

        LocalDate donationDate,
        String donationType,

        ContactTarget primaryContact,
        ContactTarget secondaryContact,

        String notice

) {

    public record ContactTarget(

            String role,
            String name,
            String address,
            String phone,
            Double latitude,
            Double longitude

    ) {
    }
}