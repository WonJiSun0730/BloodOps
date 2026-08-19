package com.shop.member.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// HTML 화면이 아니라 React에 데이터를 전달하는 컨트롤러
@RestController
@RequestMapping("/api/auth")
public class AuthController {

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

    // 현재 로그인한 사용자의 정보를 React에 반환합니다.
// 최종 주소: GET /api/auth/me
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(Authentication authentication) {

        // 로그인한 사용자가 가진 권한을 문자열 목록으로 변환합니다.
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .toList();

        // authentication.getName()에는 현재 로그인한 회원의 이메일이 들어 있습니다.
        return Map.of(
                "email", authentication.getName(),
                "roles", roles
        );
    }
}