package com.shop.changhyeon.management.dto;

import com.shop.changhyeon.management.entity.DonorConfirmation;

import java.time.LocalDateTime;

public record DonorConfirmationResponseDto(
        Long confirmationId,
        String name,
        String birthDate,
        Double height,
        Double weight,
        String bloodType,
        String phone,
        String email,
        String address,
        String signatureData,
        LocalDateTime updatedAt
) {
    public static DonorConfirmationResponseDto from(DonorConfirmation confirmation) {
        return new DonorConfirmationResponseDto(
                confirmation.getConfirmationId(),
                confirmation.getName(),
                confirmation.getBirthDate(),
                confirmation.getHeight(),
                confirmation.getWeight(),
                confirmation.getBloodType(),
                confirmation.getPhone(),
                confirmation.getEmail(),
                confirmation.getAddress(),
                confirmation.getSignatureData(),
                confirmation.getUpdatedAt()
        );
    }
}
