package com.shop.member.dto;

import java.util.Map;

public class KakaoUserInfo {
    private Map<String, Object> attributes;
    private Map<String, Object> kakaoAccount;
    private Map<String, Object> profile;

    @SuppressWarnings("unchecked")
    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (this.kakaoAccount != null) {
            this.profile = (Map<String, Object>) kakaoAccount.get("profile");
        }
    }

    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    public String getEmail() {
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }

    public String getNickname() {
        return profile != null ? (String) profile.get("nickname") : null;
    }
}
