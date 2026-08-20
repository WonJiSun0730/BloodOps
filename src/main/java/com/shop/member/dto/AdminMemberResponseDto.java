package com.shop.member.dto;

import com.shop.member.constant.LoginProvider;
import com.shop.member.constant.Role;
import com.shop.member.entity.Member;
import lombok.Getter;

@Getter
public class AdminMemberResponseDto {

    private final Long id;
    private final String loginId;
    private final String name;
    private final String email;
    private final LoginProvider provider;
    private final Role role;

    public AdminMemberResponseDto(Member member) {
        this.id = member.getId();
        this.loginId = member.getLoginId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.provider = member.getProvider();
        this.role = member.getRole();
    }
}