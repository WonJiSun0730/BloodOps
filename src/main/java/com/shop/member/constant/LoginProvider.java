package com.shop.member.constant;

/**
 * 회원이 어떤 방식(일반, 소셜)으로 가입하거나 로그인했는지 구분합니다.
 */
public enum LoginProvider {

    // 일반 아이디·비밀번호 회원
    LOCAL,

    // 소셜 로그인 회원
    GOOGLE,
    NAVER,
    KAKAO
}