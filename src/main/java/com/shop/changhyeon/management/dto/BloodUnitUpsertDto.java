package com.shop.changhyeon.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BloodUnitUpsertDto(
        @NotNull(message = "센터를 선택해 주세요.") Long centerId,
        @NotBlank(message = "헌혈자 ID를 입력해 주세요.")
        @Pattern(regexp = "[A-Za-z0-9-]+", message = "헌혈자 ID는 영문, 숫자, 하이픈만 입력할 수 있습니다.") String donorId,
        @NotBlank @Pattern(regexp = "A|B|O|AB", message = "올바른 혈액형을 선택해 주세요.") String bloodType,
        @NotBlank @Pattern(regexp = "\\+|-", message = "올바른 Rh 유형을 선택해 주세요.") String rhType,
        @NotBlank(message = "성별을 선택해 주세요.")
        @Pattern(regexp = "남|여", message = "올바른 성별을 선택해 주세요.") String gender,
        @NotNull(message = "생년월일을 입력해 주세요.") @Past(message = "생년월일은 과거 날짜여야 합니다.") LocalDate birthDate,
        @NotBlank(message = "Unit ID를 입력해 주세요.")
        @Pattern(regexp = "[A-Za-z0-9-]+", message = "Unit ID는 영문, 숫자, 하이픈만 입력할 수 있습니다.") String unitId,
        @NotBlank @Pattern(regexp = "전혈|혈장|혈소판단종|혈소판다종", message = "올바른 제제 종류를 선택해 주세요.") String productType,
        @NotNull @PastOrPresent(message = "채혈일은 현재 시각 이후로 설정할 수 없습니다.") LocalDateTime collectionDate,
        @NotNull LocalDateTime expirationDate,
        @NotBlank @Pattern(regexp = "보유중|예약|보류|폐기", message = "올바른 상태를 선택해 주세요.") String status
) {}