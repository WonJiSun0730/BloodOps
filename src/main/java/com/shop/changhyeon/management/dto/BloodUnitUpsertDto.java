package com.shop.changhyeon.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
        @NotNull @PastOrPresent(message = "채혈일은 현재 시각 이후로 설정할 수 없습니다.") LocalDateTime collectionDate,
        @NotNull LocalDateTime expirationDate,
        @NotBlank String status
) {}