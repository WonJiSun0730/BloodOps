package com.shop.common.config;

import com.shop.member.oauth.CustomOAuth2UserService;
import com.shop.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration  // 설정 클래스
@EnableWebSecurity  // 웹 보안 가능하게 하는 클래스
public class SecurityConfig {
    @Autowired
    MemberService memberService;

    @Autowired
    CustomOAuth2UserService customOAuth2UserService;

    // 허용여부 설정하는 메소드
    // @Bean 객체 -> 스프링 컨테이너 관리하고 사용하는 싱글톤
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/", "/members/**", "/item/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        )
        .formLogin(formLogin -> formLogin
                .loginPage("/members/login")
                .defaultSuccessUrl("/")
                .usernameParameter("email")
                .failureUrl("/members/login/error")
        )

        .logout(logout -> logout
                .logoutUrl("/members/logout")   // import 필요
                .logoutSuccessUrl("/")
        )

        // ---------------- [소셜 로그인 설정 추가] ---------------------------
        .oauth2Login(oauthLogin -> oauthLogin
                .defaultSuccessUrl("/")      // 로그인 성공 시 이동할 페이지
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                        .userService(customOAuth2UserService) // 소셜 사용자 정보 처리 서비스 등록
                )
        );
        // -----------------------------------------------------------------

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
        );

        return http.build();
    }
    // @Bean 객체 -> 스프링 컨테이너 관리하고 사용하는 싱글톤
    // 암호 -> 암호화 기능
    @Bean
    public static PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
    }


}
