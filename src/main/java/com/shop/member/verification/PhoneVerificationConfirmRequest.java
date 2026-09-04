package com.shop.member.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneVerificationConfirmRequest(

        @NotBlank(
                message = "휴대전화 번호를 입력해주세요."
        )
        @Pattern(
                regexp = "^010\\d{8}$",
                message = "휴대전화 번호를 정확하게 입력해주세요."
        )
        String phone,

        @NotBlank(
                message = "인증번호를 입력해주세요."
        )
        @Pattern(
                regexp = "^\\d{6}$",
                message = "인증번호는 숫자 6자리로 입력해주세요."
        )
        String code

) {
}