package com.shop.member.oauth;

import com.shop.member.constant.LoginProvider;
import com.shop.member.constant.Role;
import com.shop.member.entity.Member;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class OAuthAttributes {
    //소셜 서비스가 반환한 원본 회원정보
    private Map<String, Object> attributes;
    //사용자 식별에 사용하는 속성명
    private String nameAttributeKey;

    private String name;
    private String email;
    private String picture;

    // GOOGLE, NAVER, KAKAO 구분
    private LoginProvider provider;

    // 각 소셜 서비스가 제공하는 고유 회원 ID
    private String providerId;

    public OAuthAttributes(
            Map<String, Object> attributes,
            String nameAttributeKey,
            String name,
            String email,
            String picture,
            LoginProvider provider,
            String providerId){
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.provider = provider;
        this.providerId = providerId;
    }

    public OAuthAttributes() {
    }

    // 해당 로그인인 서비스를 구분하여, 알맞게 매핑을 해주도록 함.
    // 여기서 registrationId는 OAuth2 로그인을 처리한 서비스 명("google", "kakao", "naver" ...)이 되고,
    // userNameAttributeName은 해당 서비스의 map의 키 값이 되는 값이 됨.
    // {google="sub", kakao="id", naver="response"}
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes){
        if (registrationId.equals("kakao")){
            return ofKakao(userNameAttributeName, attributes);
        } else if (registrationId.equals("naver")){
            return ofNaver(userNameAttributeName, attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes){
        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");

        Map<String, Object> profile =
                (Map<String, Object>) kakaoAccount.get("profile");

        // 카카오의 고유 회원번호
        String providerId = String.valueOf(attributes.get("id"));

        /*
         * 카카오 이메일 제공 동의 여부와 관계없이
         * 내부에서 사용할 수 있는 고유 이메일 값을 만듭니다.
         */
        String internalEmail = providerId + "@kakao.local";

        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) profile.get("nickname"),
                internalEmail,
                (String) profile.get("profile_image_url"),
                LoginProvider.KAKAO,
                providerId
        );
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes){
        Map<String, Object> response =
                (Map<String, Object>) attributes.get("response");

        // 네이버의 고유 회원 식별값
        String providerId = String.valueOf(response.get("id"));

        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) response.get("name"),
                (String) response.get("email"),
                (String) response.get("profile_image"),
                LoginProvider.NAVER,
                providerId
        );
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes){

        // Google의 고유 회원 식별값은 sub입니다.
        String providerId = String.valueOf(attributes.get("sub"));

        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                (String) attributes.get("picture"),
                LoginProvider.GOOGLE,
                providerId
        );
    }

    /*
     * 첫 소셜 로그인 사용자를 Member 엔티티로 변환합니다.
     *
     * 소셜 회원은 일반 로그인용 아이디와 비밀번호가 없고,
     * 기본 USER 권한을 가집니다.
     */
    public Member toMember() {
        Member member = new Member();

        member.setName(name);
        member.setEmail(email);
        member.setPicture(picture);
        member.setProvider(provider);
        member.setProviderId(providerId);
        member.setRole(Role.USER);

        return member;
    }
}
