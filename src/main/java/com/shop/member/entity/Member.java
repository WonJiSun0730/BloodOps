package com.shop.member.entity;

import com.shop.common.entity.BaseEntity;
import com.shop.member.constant.LoginProvider;
import com.shop.member.constant.Role;
import com.shop.member.dto.MemberFormDto;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
//providerId 단독이 아니라 provider + providerId 조합을 고유값으로 사용
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_provider_id",
                        columnNames = {"provider", "provider_id"}
                )
        }
)
@Getter
@Setter
@ToString
public class Member extends BaseEntity {
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //일반 로그인에 사용할 아이디
    @Column(unique = true) //소셜 로그인 회원은 아이디가 없을 수 있어 nullable설정 안함
    private String loginId;

    private String name;

    @Column(unique = true)
    private String email; //일반, 소셜 모두 사용

    //소셜 회원이 없을 수 있는 정보
    private String password;

    // 생년월일
    // 화면에서는 6자리 또는 8자리로 입력받고 LocalDate로 변환할 예정
    private LocalDate birthDate;

    //폰 번호
    private String phone;

    //SMS, 이메일 수신 동의 여부
    private boolean smsAgreed;
    private boolean emailAgreed;

    /*
     * 회원의 로그인 방식을 저장
     *
     * 일반 회원은 LOCAL,
     * 소셜 회원은 GOOGLE·NAVER·KAKAO 중 하나
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginProvider provider = LoginProvider.LOCAL;

    /*
     * 소셜 서비스가 제공하는 고유 회원 ID입니다.
     * 일반 회원은 사용하지 않으므로 null입니다.
     */
    @Column(name = "provider_id")
    private String providerId;

    /*
     * 소셜 프로필 이미지 주소입니다.
     * 일반 회원은 사용하지 않으므로 null입니다.
     */
    @Column(length = 1000)
    private String picture;

    @Enumerated(EnumType.STRING)
    private Role role;

    // static 객체 안만들어도 바로 사용가능
    // MemberFormDto, PasswordEncoder
    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){
        Member member = new Member();

        member.setLoginId(memberFormDto.getLoginId());
        member.setName(memberFormDto.getName());
        member.setBirthDate(parseBirthDate(memberFormDto.getBirthDate()));
        member.setPhone(memberFormDto.getPhone());
        member.setEmail(memberFormDto.getEmail());
        member.setSmsAgreed(memberFormDto.isSmsAgreed());
        member.setEmailAgreed(memberFormDto.isEmailAgreed());

        //암호화하여 비밀번호 저장
        String encodedPassword = passwordEncoder.encode(memberFormDto.getPassword());

        member.setPassword(encodedPassword);

        // 일반 회원가입은 아이디와 비밀번호를 사용하는 LOCAL 회원
        member.setProvider(LoginProvider.LOCAL);
        //일반 회원가입 사용자는 유저 권한 자동 부여
        member.setRole(Role.USER);

        return member;
    }

    /*
     * 6자리 또는 8자리 생년월일 문자열을 LocalDate로 변환합니다.
     *
     * 950730   → 1995-07-30
     * 20050730 → 2005-07-30
     */
    private static LocalDate parseBirthDate(String birthDate) {

        String eightDigitBirthDate = birthDate;

        if (birthDate.length() == 6) {
            int inputYear = Integer.parseInt(birthDate.substring(0, 2));
            int currentYear = LocalDate.now().getYear() % 100;

            /*
             * 입력한 두 자리 연도가 현재 연도 이하라면 2000년대,
             * 현재 연도보다 크다면 1900년대로 판단합니다.
             */
            String century = inputYear <= currentYear ? "20" : "19";
            eightDigitBirthDate = century + birthDate;
        }

        return LocalDate.parse(
                eightDigitBirthDate,
                DateTimeFormatter.BASIC_ISO_DATE
        );
    }
}
