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

        Member member = memberRepository
                .findByProviderAndProviderId(
                        attributes.getProvider(),
                        attributes.getProviderId()
                )
                .orElse(null);

        /*
         * 이미 연결된 동일 소셜 계정이면
         * 정상 재로그인으로 처리합니다.
         */
        if (member != null) {
            member.setName(
                    attributes.getName()
            );

            member.setPicture(
                    attributes.getPicture()
            );

            if (
                    member.getBirthDate() == null
                            && attributes.getBirthDate() != null
                            && attributes.getBirthDate().length() == 8
            ) {
                member.setBirthDate(
                        java.time.LocalDate.parse(
                                attributes.getBirthDate(),
                                java.time.format.DateTimeFormatter.BASIC_ISO_DATE
                        )
                );
            }

            if (
                    (member.getPhone() == null
                            || member.getPhone().isBlank())
                            && attributes.getPhone() != null
                            && !attributes.getPhone().isBlank()
            ) {
                if (
                        memberRepository.existsByPhone(
                                attributes.getPhone()
                        )
                ) {
                    throw new OAuth2AuthenticationException(
                            "duplicate_phone"
                    );
                }

                member.setPhone(
                        attributes.getPhone()
                );
            }
            return memberRepository.save(
                    member
            );
        }

        /*
         * 여기부터는 "처음 들어온 소셜 계정"입니다.
         *
         * 기존 BloodOps 회원의 이메일과 겹치면
         * 자동 계정 연결하지 않고 가입을 차단합니다.
         */
        if (
                attributes.getEmail() != null
                        && !attributes.getEmail().isBlank()
                        && memberRepository.existsByEmail(
                        attributes.getEmail()
                )
        ) {
            throw new OAuth2AuthenticationException(
                    "duplicate_email"
            );
        }

        /*
         * 소셜 제공자가 휴대전화 번호를 제공한 경우,
         * 기존 회원과 번호가 겹치면 가입을 차단합니다.
         */
        if (
                attributes.getPhone() != null
                        && !attributes.getPhone().isBlank()
                        && memberRepository.existsByPhone(
                        attributes.getPhone()
                )
        ) {
            throw new OAuth2AuthenticationException(
                    "duplicate_phone"
            );
        }

        /*
         * 이메일/전화번호 모두 기존 회원과 겹치지 않는
         * 신규 소셜 회원만 생성합니다.
         */
        member = attributes.toMember();

        return memberRepository.save(
                member
        );
    }
}