package com.shop.member.verification;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String SESSION_EMAIL =
            "EMAIL_VERIFICATION_EMAIL";

    private static final String SESSION_CODE =
            "EMAIL_VERIFICATION_CODE";

    private static final String SESSION_EXPIRES_AT =
            "EMAIL_VERIFICATION_EXPIRES_AT";

    private static final String SESSION_VERIFIED =
            "EMAIL_VERIFICATION_VERIFIED";

    private static final String SESSION_LAST_SENT_AT =
            "EMAIL_VERIFICATION_LAST_SENT_AT";

    public static final long EXPIRATION_SECONDS = 5 * 60;
    public static final long RESEND_COOLDOWN_SECONDS = 2 * 60;

    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom =
            new SecureRandom();

    public void sendVerificationCode(
            String email,
            HttpSession session
    ) {

        String normalizedEmail =
                normalizeEmail(email);

        long now =
                Instant.now().getEpochSecond();

        Long lastSentAt =
                (Long) session.getAttribute(
                        SESSION_LAST_SENT_AT
                );

        if (lastSentAt != null) {

            long elapsed =
                    now - lastSentAt;

            if (elapsed < RESEND_COOLDOWN_SECONDS) {

                long remaining =
                        RESEND_COOLDOWN_SECONDS
                                - elapsed;

                throw new IllegalStateException(
                        "인증번호는 "
                                + remaining
                                + "초 후 다시 발송할 수 있습니다."
                );
            }
        }

        String code =
                String.format(
                        "%06d",
                        secureRandom.nextInt(
                                1_000_000
                        )
                );

        long expiresAt =
                now + EXPIRATION_SECONDS;

        /*
         * 새 인증번호 발송 시
         * 이전 인증번호 및 인증 완료 상태는
         * 자동으로 무효화됩니다.
         */
        session.setAttribute(
                SESSION_EMAIL,
                normalizedEmail
        );

        session.setAttribute(
                SESSION_CODE,
                code
        );

        session.setAttribute(
                SESSION_EXPIRES_AT,
                expiresAt
        );

        session.setAttribute(
                SESSION_LAST_SENT_AT,
                now
        );

        session.setAttribute(
                SESSION_VERIFIED,
                false
        );

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(normalizedEmail);

        message.setSubject(
                "[BloodOps] 이메일 인증번호 안내"
        );

        message.setText(
                """
                BloodOps 이메일 인증번호입니다.

                인증번호: %s

                인증번호는 5분 동안 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(code)
        );

        mailSender.send(message);
    }

    public void confirmVerificationCode(
            String email,
            String code,
            HttpSession session
    ) {

        String normalizedEmail =
                normalizeEmail(email);

        String savedEmail =
                (String) session.getAttribute(
                        SESSION_EMAIL
                );

        String savedCode =
                (String) session.getAttribute(
                        SESSION_CODE
                );

        Long expiresAt =
                (Long) session.getAttribute(
                        SESSION_EXPIRES_AT
                );

        if (
                savedEmail == null
                        || savedCode == null
                        || expiresAt == null
        ) {
            throw new IllegalStateException(
                    "먼저 인증번호를 발송해주세요."
            );
        }

        /*
         * 인증번호를 받은 이메일과
         * 현재 입력된 이메일이 다르면
         * 해당 인증번호를 사용할 수 없습니다.
         */
        if (
                !savedEmail.equals(
                        normalizedEmail
                )
        ) {
            throw new IllegalStateException(
                    "인증번호를 발송한 이메일과 현재 이메일이 다릅니다. 새 이메일로 인증번호를 다시 발송해주세요."
            );
        }

        long now =
                Instant.now().getEpochSecond();

        if (now > expiresAt) {

            clearVerificationCode(session);

            throw new IllegalStateException(
                    "인증번호가 만료되었습니다. 인증번호를 다시 발송해주세요."
            );
        }

        if (!savedCode.equals(code)) {
            throw new IllegalStateException(
                    "인증번호가 올바르지 않습니다."
            );
        }

        session.setAttribute(
                SESSION_VERIFIED,
                true
        );
    }

    public boolean isVerified(
            String email,
            HttpSession session
    ) {

        String normalizedEmail =
                normalizeEmail(email);

        String savedEmail =
                (String) session.getAttribute(
                        SESSION_EMAIL
                );

        Boolean verified =
                (Boolean) session.getAttribute(
                        SESSION_VERIFIED
                );

        Long expiresAt =
                (Long) session.getAttribute(
                        SESSION_EXPIRES_AT
                );

        if (
                savedEmail == null
                        || verified == null
                        || !verified
                        || expiresAt == null
        ) {
            return false;
        }

        if (
                Instant.now().getEpochSecond()
                        > expiresAt
        ) {
            clearVerificationCode(session);
            return false;
        }

        return savedEmail.equals(
                normalizedEmail
        );
    }

    public void clearVerification(
            HttpSession session
    ) {

        session.removeAttribute(
                SESSION_EMAIL
        );

        session.removeAttribute(
                SESSION_CODE
        );

        session.removeAttribute(
                SESSION_EXPIRES_AT
        );

        session.removeAttribute(
                SESSION_VERIFIED
        );

        session.removeAttribute(
                SESSION_LAST_SENT_AT
        );
    }

    private void clearVerificationCode(
            HttpSession session
    ) {

        session.removeAttribute(
                SESSION_CODE
        );

        session.removeAttribute(
                SESSION_EXPIRES_AT
        );

        session.setAttribute(
                SESSION_VERIFIED,
                false
        );
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null) {
            return "";
        }

        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }
}