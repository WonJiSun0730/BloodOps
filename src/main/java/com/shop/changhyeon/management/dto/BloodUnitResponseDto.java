package com.shop.changhyeon.management.dto;

import com.shop.changhyeon.management.entity.BloodUnit;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BloodUnitResponseDto(
        Long id,
        String donorId,
        String bloodType,
        String rhType,
        String gender,
        LocalDate birthDate,
        String unitId,
        String productType,
        LocalDateTime collectionDate,
        LocalDateTime expirationDate,
        String status
) {
    public static BloodUnitResponseDto from(BloodUnit unit) {
        return new BloodUnitResponseDto(
                unit.getId(),
                unit.getDonorId(),
                unit.getBloodType(),
                unit.getRhType(),
                unit.getGender(),
                unit.getBirthDate(),
                unit.getUnitId(),
                unit.getProductType(),
                unit.getCollectionDate(),
                unit.getExpirationDate(),
                unit.getStatus()
        );
    }
}
