package com.shop.changhyeon.management.dto;

public record DonorIdentityResponseDto(
        Long confirmationId,
        boolean existingDonor,
        String maskedResidentRegistrationNumber,
        DonorConfirmationResponseDto donor
) {}
