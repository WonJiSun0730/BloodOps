package com.shop.common.config;

import com.shop.member.constant.Role;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@Profile("local")
@ConditionalOnProperty(
        name = "bloodops.test-accounts.enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class LocalTestAccountInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /*
     * application-local.properties의 값을 가져옵니다.
     * 환경변수가 없다면 Test1234!를 사용합니다.
     */
    @Value("${bloodops.test-accounts.password}")
    private String testPassword;

    /*
     * Spring Boot 실행과 DB 테이블 생성이 끝난 후 자동으로 실행됩니다.
     */
    @Override
    public void run(String... args) {
        createTestAccount(
                "testuser",
                "일반 사용자",
                "user@bloodops.local",
                "01011111111",
                LocalDate.of(1995, 7, 30),
                Role.USER
        );

        createTestAccount(
                "testmedical",
                "테스트 의료인",
                "medical@bloodops.local",
                "01022222222",
                LocalDate.of(1990, 1, 1),
                Role.MEDICAL
        );

        createTestAccount(
                "testadmin",
                "테스트 관리자",
                "admin@bloodops.local",
                "01033333333",
                LocalDate.of(1985, 1, 1),
                Role.ADMIN
        );

        log.info("BloodOps local 테스트 계정 생성 완료");
    }

    private void createTestAccount(
            String loginId,
            String name,
            String email,
            String phone,
            LocalDate birthDate,
            Role role
    ) {
        /*
         * ddl-auto 설정이 나중에 create에서 update로 바뀌어도
         * 같은 테스트 계정이 중복 생성되지 않도록 확인합니다.
         */
        if (memberRepository.existsByLoginId(loginId)) {
            return;
        }

        Member member = new Member();
        member.setLoginId(loginId);
        member.setName(name);
        member.setEmail(email);
        member.setPhone(phone);
        member.setBirthDate(birthDate);

        // 테스트 계정도 실제 회원과 동일하게 암호화해서 저장합니다.
        member.setPassword(passwordEncoder.encode(testPassword));

        member.setSmsAgreed(false);
        member.setEmailAgreed(false);
        member.setRole(role);

        memberRepository.save(member);

        log.info("테스트 계정 생성: loginId={}, role={}", loginId, role);
    }
}