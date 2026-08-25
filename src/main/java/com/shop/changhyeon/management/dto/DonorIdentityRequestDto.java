package com.shop.changhyeon.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DonorIdentityRequestDto(
        @NotBlank String name,
        @NotBlank
        @Pattern(regexp = "\\d{6}-?\\d{7}", message = "주민등록번호 형식을 확인해 주세요.")
        String residentRegistrationNumber
) {}
