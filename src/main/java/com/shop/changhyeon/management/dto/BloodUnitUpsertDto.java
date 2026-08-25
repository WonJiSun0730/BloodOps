package com.shop.changhyeon.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BloodUnitUpsertDto(
        String donorId,
        @NotBlank String bloodType,
        @NotBlank String rhType,
        String gender,
        LocalDate birthDate,
        @NotBlank String unitId,
        @NotBlank String productType,
        @NotNull LocalDateTime collectionDate,
        @NotNull LocalDateTime expirationDate,
        @NotBlank String status
) {}
