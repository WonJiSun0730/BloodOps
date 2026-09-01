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
        "/api/members/email-verification"
)
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService
            emailVerificationService;

    @PostMapping("/send")
    public ResponseEntity<?> send(
            @Valid
            @RequestBody
            EmailVerificationRequest request,
            HttpSession session
    ) {

        try {

            emailVerificationService
                    .sendVerificationCode(
                            request.email(),
                            session
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "인증번호를 발송했습니다.",
                            "expiresInSeconds",
                            EmailVerificationService
                                    .EXPIRATION_SECONDS,
                            "resendAvailableInSeconds",
                            EmailVerificationService
                                    .RESEND_COOLDOWN_SECONDS
                    )
            );

        } catch (IllegalStateException e) {

            /*
             * 재발송 제한은 사용자가 예상할 수 있는
             * 정상적인 요청 상태이므로 500이 아닙니다.
             */
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
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(
            @Valid
            @RequestBody
            EmailVerificationConfirmRequest request,
            HttpSession session
    ) {

        try {

            emailVerificationService
                    .confirmVerificationCode(
                            request.email(),
                            request.code(),
                            session
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "이메일 인증이 완료되었습니다."
                    )
            );

        } catch (IllegalStateException e) {

            /*
             * 만료 / 이메일 불일치 / 잘못된 인증번호는
             * 서버 장애가 아니라 입력 상태 오류이므로
             * 400으로 반환합니다.
             */
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