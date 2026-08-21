package com.shop.changhyeon.management.dto;

public record BloodUnitRegisterDto(
        String donorId,
        String bloodType,
        String rhType,
        String gender,
        String birthDate,
        String productType
) {}