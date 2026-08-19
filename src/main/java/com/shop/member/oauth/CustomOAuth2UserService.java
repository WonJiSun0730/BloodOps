package com.shop.member.oauth;

import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
                new DefaultOAuth2UserService();

        // Google·Naver·Kakao로부터 원본 회원정보를 받아옵니다.
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        // 현재 로그인에 사용된 서비스명입니다.
        String registrationId =
                userRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName =
                userRequest.getClientRegistration()
                        .getProviderDetails()
                        .getUserInfoEndpoint()
                        .getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId,
                userNameAttributeName,
                oauth2User.getAttributes()
        );

        // 소셜 회원을 member 테이블에서 조회하거나 새로 저장합니다.
        Member member = saveOrUpdate(attributes);

        /*
         * 소셜 서비스마다 사용자 식별 속성 구조가 다르므로
         * BloodOps에서 공통으로 사용할 식별 속성을 추가합니다.
         */
        Map<String, Object> normalizedAttributes =
                new HashMap<>(attributes.getAttributes());

        normalizedAttributes.put(
                "bloodops_identifier",
                member.getEmail()
        );

        /*
         * DB에 저장된 실제 권한을 사용합니다.
         * 기존처럼 ROLE_USER로 고정하지 않습니다.
         */
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" + member.getRole().name()
                );

        return new DefaultOAuth2User(
                Collections.singleton(authority),
                normalizedAttributes,
                "bloodops_identifier"
        );
    }

    private Member saveOrUpdate(OAuthAttributes attributes) {

        /*
         * 가장 먼저 제공자와 제공자 고유 ID로 회원을 찾습니다.
         * 소셜 이메일이 변경돼도 동일 회원을 찾을 수 있습니다.
         */
        Member member = memberRepository
                .findByProviderAndProviderId(
                        attributes.getProvider(),
                        attributes.getProviderId()
                )
                .orElse(null);

        /*
         * 제공자 ID로 찾지 못했지만 같은 이메일의 회원이 있다면
         * 기존 계정을 사용합니다.
         *
         * 일반 계정과 소셜 계정의 이메일이 같은 경우
         * 회원 레코드가 중복 생성되는 것을 방지합니다.
         */
        if (member == null) {
            member = memberRepository.findByEmail(
                    attributes.getEmail()
            );
        }

        // 처음 로그인한 소셜 사용자라면 새 Member를 만듭니다.
        if (member == null) {
            member = attributes.toMember();
        } else {
            // 다시 로그인하면 변경될 수 있는 프로필 정보만 갱신합니다.
            member.setName(attributes.getName());
            member.setPicture(attributes.getPicture());

            /*
             * 기존 회원에게 제공자 정보가 없는 경우에만 연결합니다.
             * LOCAL 회원의 로그인 방식은 유지합니다.
             */
            if (member.getProvider() == null) {
                member.setProvider(attributes.getProvider());
                member.setProviderId(attributes.getProviderId());
            }
        }

        return memberRepository.save(member);
    }
}