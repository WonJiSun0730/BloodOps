package com.shop.member.dto;

import java.util.Map;

public class NaverUserInfo {
    private Map<String, Object> attributes;
    private Map<String, Object> response;

    @SuppressWarnings("unchecked")
    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        // 네이버는 사용자 정보가 "response" 키 안에 들어가 있습니다.
        this.response = (Map<String, Object>) attributes.get("response");
    }

    public String getProviderId() {
        return response != null ? (String) response.get("id") : null;
    }

    public String getEmail() {
        return response != null ? (String) response.get("email") : null;
    }

    public String getName() {
        return response != null ? (String) response.get("name") : null;
    }

    public String getNickname() {
        return response != null ? (String) response.get("nickname") : null;
    }
}
