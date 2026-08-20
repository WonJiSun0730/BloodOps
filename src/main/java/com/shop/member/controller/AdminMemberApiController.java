package com.shop.member.controller;

import com.shop.member.constant.Role;
import com.shop.member.dto.AdminMemberResponseDto;
import com.shop.member.dto.AdminMemberRoleUpdateDto;
import com.shop.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberApiController {

    private final MemberService memberService;

    /*
     * ADMIN 회원 관리 화면의 전체 회원 목록을 조회합니다.
     *
     * GET /api/admin/members
     */
    @GetMapping
    public List<AdminMemberResponseDto> getMembers() {
        return memberService.getAdminMemberList();
    }

    /*
     * ADMIN이 회원의 권한을 변경합니다.
     *
     * PUT /api/admin/members/{memberId}/role
     */
    @PutMapping("/{memberId}/role")
    public ResponseEntity<?> updateMemberRole(
            @PathVariable Long memberId,
            @RequestBody AdminMemberRoleUpdateDto request,
            Authentication authentication
    ) {
        try {
            AdminMemberResponseDto member =
                    memberService.updateMemberRole(
                            memberId,
                            request.getRole()
                    );

            boolean changedSelf =
                    authentication.getName()
                            .equals(member.getEmail());

            boolean lostAdminAccess =
                    changedSelf
                            && member.getRole() != Role.ADMIN;

            /*
             * 현재 로그인한 사용자가 자기 자신의 권한을 변경했다면
             * DB뿐 아니라 현재 Spring Security 인증 권한도 즉시 갱신합니다.
             */
            if (changedSelf) {
                SimpleGrantedAuthority newAuthority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + member.getRole().name()
                        );

                Authentication updatedAuthentication;

                /*
                 * 소셜 로그인 사용자라면 OAuth 인증 타입을 유지합니다.
                 */
                if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                    OAuth2AuthenticationToken updatedOAuthToken =
                            new OAuth2AuthenticationToken(
                                    oauthToken.getPrincipal(),
                                    java.util.List.of(newAuthority),
                                    oauthToken.getAuthorizedClientRegistrationId()
                            );

                    updatedOAuthToken.setDetails(
                            oauthToken.getDetails()
                    );

                    updatedAuthentication =
                            updatedOAuthToken;

                } else {
                    /*
                     * 일반 로그인 사용자
                     */
                    UsernamePasswordAuthenticationToken updatedToken =
                            new UsernamePasswordAuthenticationToken(
                                    authentication.getPrincipal(),
                                    authentication.getCredentials(),
                                    java.util.List.of(newAuthority)
                            );

                    updatedToken.setDetails(
                            authentication.getDetails()
                    );

                    updatedAuthentication =
                            updatedToken;
                }

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(updatedAuthentication);
            }

            return ResponseEntity.ok(
                    Map.of(
                            "member", member,
                            "adminAccessLost", lostAdminAccess
                    )
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            e.getMessage()
                    )
            );
        }
    }
}