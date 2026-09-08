package com.shop.member.verification;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class PhoneVerificationService {

    private static final Logger log =
            LoggerFactory.getLogger(PhoneVerificationService.class);

    private static final String SESSION_PHONE =
            "PHONE_VERIFICATION_PHONE";

    private static final String SESSION_CODE =
            "PHONE_VERIFICATION_CODE";

    private static final String SESSION_CODE_EXPIRES_AT =
            "PHONE_VERIFICATION_CODE_EXPIRES_AT";

    private static final String SESSION_VERIFIED =
            "PHONE_VERIFICATION_VERIFIED";

    private static final String SESSION_LAST_SENT_AT =
            "PHONE_VERIFICATION_LAST_SENT_AT";

    public static final long EXPIRATION_SECONDS = 5 * 60;
    public static final long RESEND_COOLDOWN_SECONDS = 60;

    private final boolean enabled;
    private final String apiKey;
    private final String apiSecret;
    private final String sender;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public PhoneVerificationService(
            @Value("${solapi.enabled:false}") boolean enabled,
            @Value("${solapi.api-key:}") String apiKey,
            @Value("${solapi.api-secret:}") String apiSecret,
            @Value("${solapi.sender:}") String sender
    ) {
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.sender = sender;
    }

    public void sendVerificationCode(
            String phone,
            HttpSession session
    ) {
        String normalizedPhone =
                normalizePhone(phone);

        validatePhone(normalizedPhone);

        if (!enabled) {
            throw new IllegalStateException(
                    "문자 인증 서비스를 사용할 수 없습니다."
            );
        }

        if (
                apiKey.isBlank()
                        || apiSecret.isBlank()
                        || sender.isBlank()
        ) {
            throw new IllegalStateException(
                    "문자 인증 서비스 설정을 확인해주세요."
            );
        }

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
                        RESEND_COOLDOWN_SECONDS - elapsed;

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
         * 이전 인증 성공 상태도 무효화합니다.
         */
        session.setAttribute(
                SESSION_PHONE,
                normalizedPhone
        );

        session.setAttribute(
                SESSION_CODE,
                code
        );

        session.setAttribute(
                SESSION_CODE_EXPIRES_AT,
                expiresAt
        );

        session.setAttribute(
                SESSION_VERIFIED,
                false
        );

        try {
            DefaultMessageService messageService =
                    SolapiClient.INSTANCE.createInstance(
                            apiKey,
                            apiSecret
                    );

            Message message =
                    new Message();

            message.setFrom(
                    normalizePhone(sender)
            );

            message.setTo(
                    normalizedPhone
            );

            message.setText(
                    """
                    [BloodOps]
                    휴대전화 인증번호는 [%s]입니다.
                    인증번호는 5분 동안 유효합니다.
                    """.formatted(code)
            );

            messageService.send(
                    message,
                    null
            );

            /*
             * 실제 발송 요청이 성공한 뒤에만
             * 재발송 제한 시간을 기록합니다.
             *
             * 발송 자체가 실패했는데 60초 동안
             * 사용자를 막는 상황을 방지합니다.
             */
            session.setAttribute(
                    SESSION_LAST_SENT_AT,
                    now
            );

        } catch (Exception exception) {

            /*
             * API Key, Secret, 인증번호 등의 민감정보는
             * 로그에 직접 출력하지 않습니다.
             */
            log.error(
                    "[휴대전화 인증] SOLAPI 발송 실패 - type={}, message={}",
                    exception.getClass().getName(),
                    exception.getMessage()
            );

            /*
             * 발송 실패 시 방금 생성한 인증정보는
             * 사용할 수 없도록 제거합니다.
             */
            clearPendingVerification(session);

            throw new IllegalStateException(
                    "인증번호 발송에 실패했습니다. 잠시 후 다시 시도해주세요."
            );
        }
    }

    public void confirmVerificationCode(
            String phone,
            String code,
            HttpSession session
    ) {
        String normalizedPhone =
                normalizePhone(phone);

        validatePhone(normalizedPhone);

        String savedPhone =
                (String) session.getAttribute(
                        SESSION_PHONE
                );

        String savedCode =
                (String) session.getAttribute(
                        SESSION_CODE
                );

        Long expiresAt =
                (Long) session.getAttribute(
                        SESSION_CODE_EXPIRES_AT
                );

        if (
                savedPhone == null
                        || savedCode == null
                        || expiresAt == null
        ) {
            throw new IllegalStateException(
                    "먼저 인증번호를 발송해주세요."
            );
        }

        if (!savedPhone.equals(normalizedPhone)) {
            throw new IllegalStateException(
                    "인증번호를 발송한 휴대전화 번호와 현재 번호가 다릅니다. 인증번호를 다시 발송해주세요."
            );
        }

        long now =
                Instant.now().getEpochSecond();

        if (now > expiresAt) {
            clearPendingVerification(session);

            throw new IllegalStateException(
                    "인증번호가 만료되었습니다. 인증번호를 다시 발송해주세요."
            );
        }

        String normalizedCode =
                code == null
                        ? ""
                        : code.trim();

        if (!savedCode.equals(normalizedCode)) {
            throw new IllegalStateException(
                    "인증번호가 올바르지 않습니다."
            );
        }

        /*
         * 여기서부터는 인증번호의 5분 만료시간과
         * 인증 완료 상태를 분리합니다.
         *
         * 인증 성공 후 회원가입 작성 시간이 길어져도
         * 다시 문자 인증을 요구하지 않습니다.
         */
        session.setAttribute(
                SESSION_VERIFIED,
                true
        );

        session.removeAttribute(
                SESSION_CODE
        );

        session.removeAttribute(
                SESSION_CODE_EXPIRES_AT
        );
    }

    public boolean isVerified(
            String phone,
            HttpSession session
    ) {
        String normalizedPhone =
                normalizePhone(phone);

        String savedPhone =
                (String) session.getAttribute(
                        SESSION_PHONE
                );

        Boolean verified =
                (Boolean) session.getAttribute(
                        SESSION_VERIFIED
                );

        /*
         * 인증 성공 후에는 코드 만료시간을 검사하지 않습니다.
         *
         * 단, 최종 회원가입에 사용되는 번호와
         * 실제 인증받은 번호가 정확히 같아야 합니다.
         */
        return Boolean.TRUE.equals(verified)
                && savedPhone != null
                && savedPhone.equals(normalizedPhone);
    }

    public void clearVerification(
            HttpSession session
    ) {
        session.removeAttribute(
                SESSION_PHONE
        );

        session.removeAttribute(
                SESSION_CODE
        );

        session.removeAttribute(
                SESSION_CODE_EXPIRES_AT
        );

        session.removeAttribute(
                SESSION_VERIFIED
        );

        session.removeAttribute(
                SESSION_LAST_SENT_AT
        );
    }

    private void clearPendingVerification(
            HttpSession session
    ) {
        session.removeAttribute(
                SESSION_CODE
        );

        session.removeAttribute(
                SESSION_CODE_EXPIRES_AT
        );

        session.setAttribute(
                SESSION_VERIFIED,
                false
        );
    }

    private String normalizePhone(
            String phone
    ) {
        if (phone == null) {
            return "";
        }

        return phone.replaceAll(
                "[^0-9]",
                ""
        );
    }

    private void validatePhone(
            String phone
    ) {
        if (!phone.matches("^010\\d{8}$")) {
            throw new IllegalStateException(
                    "휴대전화 번호를 정확하게 입력해주세요."
            );
        }
    }
}