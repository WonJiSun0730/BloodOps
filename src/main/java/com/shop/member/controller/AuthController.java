package com.shop.member.controller;

import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// HTML 화면이 아니라 React에 데이터를 전달하는 컨트롤러
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberRepository memberRepository;

    // React가 로그인 요청 전에 CSRF 토큰을 받아가는 주소
    // 최종 주소: GET /api/auth/csrf
    @GetMapping("/csrf")
    public Map<String, String> getCsrfToken(CsrfToken csrfToken) {

        // React에서 필요한 토큰값과 헤더 이름을 JSON으로 반환
        return Map.of(
                "token", csrfToken.getToken(),
                "headerName", csrfToken.getHeaderName()
        );
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(
            Authentication authentication
    ) {

        List<String> roles =
                authentication.getAuthorities()
                        .stream()
                        .map(authority ->
                                authority.getAuthority()
                        )
                        .toList();

        String identifier =
                authentication.getName();

        Member member =
                memberRepository.findByEmail(identifier);

        if (member == null) {
            throw new IllegalStateException(
                    "회원 정보를 찾을 수 없습니다."
            );
        }

        boolean additionalInfoRequired =
                member.getBirthDate() == null
                        || member.getPhone() == null
                        || member.getPhone().isBlank();

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "identifier",
                identifier
        );

        response.put(
                "roles",
                roles
        );

        response.put(
                "additionalInfoRequired",
                additionalInfoRequired
        );

        response.put(
                "provider",
                member.getProvider() != null
                        ? member.getProvider().name()
                        : null
        );

        response.put(
                "name",
                member.getName()
        );

        response.put(
                "email",
                member.getEmail()
        );

        response.put(
                "birthDate",
                member.getBirthDate() != null
                        ? member.getBirthDate()
                        .format(
                                DateTimeFormatter.BASIC_ISO_DATE
                        )
                        : null
        );

        response.put(
                "phone",
                member.getPhone()
        );

        response.put(
                "smsAgreed",
                member.isSmsAgreed()
        );

        response.put(
                "emailAgreed",
                member.isEmailAgreed()
        );

        return response;
    }
}