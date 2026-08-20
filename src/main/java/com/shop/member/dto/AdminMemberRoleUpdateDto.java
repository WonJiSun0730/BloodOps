package com.shop.member.dto;

import com.shop.member.constant.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMemberRoleUpdateDto {

    private Role role;
}