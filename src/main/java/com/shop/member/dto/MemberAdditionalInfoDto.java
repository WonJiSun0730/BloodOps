package com.shop.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberAdditionalInfoDto {
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식으로 입력해주세요.")
    private String email;

    @NotBlank(message = "생년월일을 입력해주세요.")
    @Pattern(
            regexp = "^(\\d{6}|\\d{8})$",
            message = "생년월일은 숫자 6자리 또는 8자리로 입력해주세요."
    )
    private String birthDate;

    @NotBlank(message = "휴대전화를 입력해주세요.")
    @Pattern(
            regexp = "^010\\d{8}$",
            message = "휴대전화 번호를 정확하게 입력해주세요."
    )
    private String phone;

    private boolean smsAgreed;

    private boolean emailAgreed;
}