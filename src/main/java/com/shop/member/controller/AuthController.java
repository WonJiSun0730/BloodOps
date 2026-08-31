package com.shop.member.controller;

import com.shop.member.entity.Member;
import com.shop.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// HTML 화면이 아니라 React에 데이터를 전달하는 컨트롤러
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;

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

    @PostMapping("/oauth-return-to")
    public void saveOAuthReturnTo(
            @RequestParam String returnTo,
            HttpSession session
    ) {
        String safeReturnTo =
                returnTo != null
                        && returnTo.startsWith("/")
                        && !returnTo.startsWith("//")
                        ? returnTo
                        : "/";

        session.setAttribute(
                "OAUTH_RETURN_TO",
                safeReturnTo
        );
    }

    @GetMapping("/oauth-return-to")
    public Map<String, String> getOAuthReturnTo(
            HttpSession session
    ) {
        Object savedReturnTo =
                session.getAttribute(
                        "OAUTH_RETURN_TO"
                );

        String returnTo =
                savedReturnTo instanceof String value
                        && value.startsWith("/")
                        && !value.startsWith("//")
                        ? value
                        : "/";

        session.removeAttribute(
                "OAUTH_RETURN_TO"
        );

        return Map.of(
                "returnTo",
                returnTo
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
                memberService
                        .findByAuthenticationIdentifier(
                                identifier
                        );

        boolean additionalInfoRequired =
                member.getBirthDate() == null
                        || member.getPhone() == null
                        || member.getPhone().isBlank()
                        || member.getEmail() == null
                        || member.getEmail().isBlank();

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