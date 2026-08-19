package com.shop.member.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class MemberFormDto {
    @NotBlank(message = "아이디를 입력해주세요.")
    @Pattern(regexp = "^[a-z0-9]{5,20}$",
    message = "아이디는 영문 소문자와 숫자를 사용하여 5~20자로 입력해주세요.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Length(min = 8, max = 16, message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @NotBlank(message = "성명을 입력해주세요.")
    @Size(max=50, message = "성명은 50자 이하로 입력해주세요.")
    private String name;

    @NotBlank(message = "생년월일을 입력해주세요.")
    @Pattern(regexp = "^(\\d{6}|\\d{8})$",
        message = "생년월일은 숫자 6자리 또는 8자리로 입력해주세요.")
    private String birthDate;

    /*
     * React에서 하이픈을 제거한 후 전송합니다.
     * 예: 01012345678
     */
    @NotBlank(message = "휴대전화를 입력해주세요.")
    @Pattern(
            regexp = "^010\\d{8}$",
            message = "휴대전화 번호를 정확하게 입력해주세요."
    )
    private String phone;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식으로 입력해주세요.")
    private String email;

    private boolean smsAgreed;

    private boolean emailAgreed;

    /*
     * 두 비밀번호가 일치하는지 서버에서도 최종 확인합니다.
     * React의 실시간 확인만으로는 요청 데이터를 조작할 수 있기 때문에
     * 서버 검증도 반드시 필요합니다.
     */
    @AssertTrue(message = "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordMatching() {
        if (password == null || passwordConfirm == null) {
            return false;
        }

        return password.equals(passwordConfirm);
    }
}
