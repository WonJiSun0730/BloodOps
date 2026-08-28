package com.shop.member.controller;

import com.shop.member.dto.MemberAdditionalInfoDto;
import com.shop.member.dto.MemberFormDto;
import com.shop.member.entity.Member;
import com.shop.member.service.MemberService;
import com.shop.member.verification.EmailVerificationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    private final EmailVerificationService
            emailVerificationService;

    private final PasswordEncoder passwordEncoder;

    /*
     * React 회원가입 요청을 처리합니다.
     *
     * 최종 주소:
     * POST /api/members/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(
            @Valid @RequestBody MemberFormDto memberFormDto,
            BindingResult bindingResult
    ) {
        /*
         * DTO 검증에 실패한 경우 각 입력칸의 오류를 모아 반환합니다.
         */
        if (bindingResult.hasErrors()) {
            Map<String, String> fieldErrors = new LinkedHashMap<>();

            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                String fieldName = fieldError.getField();

                /*
                 * @AssertTrue로 검사한 비밀번호 일치 오류는
                 * React의 passwordConfirm 입력칸에 표시합니다.
                 */
                if ("passwordMatching".equals(fieldName)) {
                    fieldName = "passwordConfirm";
                }

                /*
                 * 같은 입력칸에 오류가 여러 개라면
                 * 첫 번째 오류 메시지만 사용합니다.
                 */
                fieldErrors.putIfAbsent(
                        fieldName,
                        fieldError.getDefaultMessage()
                );
            }

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message", "입력 내용을 확인해주세요.",
                            "errors", fieldErrors
                    )
            );
        }

        try {
            Member member =
                    Member.createMember(memberFormDto, passwordEncoder);

            memberService.saveMember(member);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "회원가입이 완료되었습니다."
                    )
            );

        } catch (DateTimeException e) {
            /*
             * 19950230처럼 자리수는 맞지만
             * 실제로 존재하지 않는 날짜인 경우입니다.
             */
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message", "입력 내용을 확인해주세요.",
                            "errors", Map.of(
                                    "birthDate",
                                    "존재하지 않는 생년월일입니다."
                            )
                    )
            );

        } catch (IllegalStateException e) {
            /*
             * 아이디 또는 이메일이 이미 사용 중인 경우입니다.
             */
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message", e.getMessage()
                    )
            );
        }
    }

    /*
     * 아이디 중복 확인
     *
     * 예:
     * GET /api/members/check-login-id?loginId=blooduser
     */
    @GetMapping("/check-login-id")
    public Map<String, Boolean> checkLoginId(
            @RequestParam String loginId
    ) {
        boolean available =
                memberService.isLoginIdAvailable(loginId);

        return Map.of("available", available);
    }

    /*
     * 이메일 중복 확인
     *
     * 예:
     * GET /api/members/check-email?email=user@naver.com
     */
    @GetMapping("/check-email")
    public Map<String, Boolean> checkEmail(
            @RequestParam String email
    ) {
        boolean available =
                memberService.isEmailAvailable(email);

        return Map.of("available", available);
    }

    @PutMapping("/additional-info")
    public ResponseEntity<Map<String, Object>> updateAdditionalInfo(
            @Valid @RequestBody MemberAdditionalInfoDto dto,
            BindingResult bindingResult,
            Authentication authentication,
            HttpSession session
    ) {

        if (bindingResult.hasErrors()) {
            Map<String, String> fieldErrors =
                    new LinkedHashMap<>();

            for (FieldError fieldError
                    : bindingResult.getFieldErrors()) {

                fieldErrors.putIfAbsent(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                );
            }

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "입력 내용을 확인해주세요.",
                            "errors",
                            fieldErrors
                    )
            );
        }

        try {
            if (
                    !emailVerificationService
                            .isVerified(
                                    dto.getEmail(),
                                    session
                            )
            ) {
                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "이메일 인증이 필요합니다.",
                                        "errors",
                                        Map.of(
                                                "email",
                                                "이메일 인증을 완료해주세요."
                                        )
                                )
                        );
            }
            memberService.updateAdditionalInfo(
                    authentication.getName(),
                    dto
            );

            emailVerificationService
                    .clearVerification(
                            session
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "추가 정보가 저장되었습니다."
                    )
            );

        } catch (DateTimeException e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "입력 내용을 확인해주세요.",
                            "errors",
                            Map.of(
                                    "birthDate",
                                    "존재하지 않는 생년월일입니다."
                            )
                    )
            );

        } catch (IllegalArgumentException e) {

            if (
                    e.getMessage() != null
                            && e.getMessage()
                            .startsWith("생년월일")
            ) {
                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "입력 내용을 확인해주세요.",
                                        "errors",
                                        Map.of(
                                                "birthDate",
                                                e.getMessage()
                                        )
                                )
                        );
            }

            if (
                    e.getMessage() != null
                            && e.getMessage()
                            .startsWith("이메일")
            ) {
                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "입력 내용을 확인해주세요.",
                                        "errors",
                                        Map.of(
                                                "email",
                                                e.getMessage()
                                        )
                                )
                        );
            }

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            e.getMessage()
                    )
            );
        }
    }
}