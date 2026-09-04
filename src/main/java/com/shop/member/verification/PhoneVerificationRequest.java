package com.shop.member.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneVerificationRequest(

        @NotBlank(
                message = "휴대전화 번호를 입력해주세요."
        )
        @Pattern(
                regexp = "^010\\d{8}$",
                message = "휴대전화 번호를 정확하게 입력해주세요."
        )
        String phone

) {
}