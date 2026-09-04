package com.shop.member.verification;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(
        "/api/members/phone-verification"
)
@RequiredArgsConstructor
public class PhoneVerificationController {

    private final PhoneVerificationService
            phoneVerificationService;

    @PostMapping("/send")
    public ResponseEntity<?> send(
            @Valid
            @RequestBody
            PhoneVerificationRequest request,
            HttpSession session
    ) {
        try {
            phoneVerificationService
                    .sendVerificationCode(
                            request.phone(),
                            session
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "인증번호를 발송했습니다.",
                            "expiresInSeconds",
                            PhoneVerificationService
                                    .EXPIRATION_SECONDS,
                            "resendAvailableInSeconds",
                            PhoneVerificationService
                                    .RESEND_COOLDOWN_SECONDS
                    )
            );

        } catch (IllegalStateException e) {

            /*
             * 현재 서비스에서는 재발송 제한뿐 아니라
             * SOLAPI 설정/발송 실패도 같은 예외를 사용합니다.
             *
             * 재발송 제한인 경우만 429,
             * 그 외 사용자가 처리 가능한 오류는 400으로 반환합니다.
             */
            if (
                    e.getMessage() != null
                            && e.getMessage()
                            .contains("초 후 다시 발송")
            ) {
                return ResponseEntity
                        .status(
                                HttpStatus.TOO_MANY_REQUESTS
                        )
                        .body(
                                Map.of(
                                        "message",
                                        e.getMessage()
                                )
                        );
            }

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(
            @Valid
            @RequestBody
            PhoneVerificationConfirmRequest request,
            HttpSession session
    ) {
        try {
            phoneVerificationService
                    .confirmVerificationCode(
                            request.phone(),
                            request.code(),
                            session
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "휴대전화 인증이 완료되었습니다."
                    )
            );

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}