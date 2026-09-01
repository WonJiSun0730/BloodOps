package com.shop.member.verification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailVerificationConfirmRequest(

        @NotBlank(
                message = "이메일을 입력해주세요."
        )
        @Email(
                message = "올바른 이메일 형식으로 입력해주세요."
        )
        String email,

        @NotBlank(
                message = "인증번호를 입력해주세요."
        )
        @Pattern(
                regexp = "\\d{6}",
                message = "인증번호는 숫자 6자리입니다."
        )
        String code
) {
}