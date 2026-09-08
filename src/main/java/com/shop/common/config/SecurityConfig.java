package com.shop.common.config;

import com.shop.member.entity.Member;
import com.shop.member.oauth.CustomOAuth2UserService;
import com.shop.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

@Configuration  // 설정 클래스
@EnableWebSecurity  // 웹 보안 가능하게 하는 클래스
public class SecurityConfig {

    // application.properties에 설정한 React 주소를 가져옵니다.
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Autowired
    MemberService memberService;

    @Autowired
    CustomOAuth2UserService customOAuth2UserService;

    // 허용여부 설정하는 메소드
    // @Bean 객체 -> 스프링 컨테이너 관리하고 사용하는 싱글톤
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/uploads/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/", "/item/**", "/images/**").permitAll()
                        .requestMatchers(
                                "/api/public/**",
                                "/api/promotions",
                                "/api/promotions/*",
                                "/api/promotions/*/image",

                                // 로그인 관련 공개 API
                                "/api/auth/csrf",
                                "/api/auth/login",
                                "/api/auth/oauth-return-to",
                                // 발급번호를 가진 사람은 로그인 없이 진위여부를 확인할 수 있습니다.
                                "/api/certificates/verify",

                                // React 회원가입 관련 공개 API
                                "/api/members/signup",
                                "/api/members/check-login-id",
                                "/api/members/check-email",
                                "/api/members/email-verification/**",
                                "/api/members/phone-verification/**",

                                // 비회원도 전자문진 문항 조회와 제출 가능
                                "/api/questionnaires/questions",
                                "/api/questionnaires",

                                // 사이트 이용 안내 AI 챗봇
                                "/api/chatbot/**"
                        ).permitAll()
                        // 비회원도 헌혈센터 목록과 상세 정보를 조회할 수 있습니다.
                        // 헌혈팁 목록·최신글·상세글도 비회원 조회가 가능합니다.
                        // QnA 질문은 본인 또는 관리자에게만 공개합니다.
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/centers",
                                "/api/centers/**",
                                "/api/donation-tips",
                                "/api/donation-tips/**"
                        ).permitAll()
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/donation-tips",
                                "/api/donation-tips/images"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/api/donation-tips/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/api/donation-tips/**"
                        ).hasRole("ADMIN")
                        // 관리자 전용 화면 및 API
                        .requestMatchers(
                                "/admin/**",
                                "/api/admin/**"
                        ).hasRole("ADMIN")
                        // 의료인과 관리자만 사용할 수 있는 화면 및 API
                        .requestMatchers(
                                "/medical/**",
                                "/api/medical/**"
                        ).hasAnyRole("MEDICAL", "ADMIN")
                .anyRequest().authenticated()
        )

        .formLogin(formLogin -> formLogin

                // React가 이메일과 비밀번호를 전송할 주소입니다.
                // 이 주소를 MemberController가 아니라 Spring Security가 직접 처리합니다.
                .loginProcessingUrl("/api/auth/login")

                // 로그인할 때 사용자 아이디로 사용할 입력값의 이름입니다.
                // React 로그인 폼에서 loginId라는 이름으로 전송한 값을 일반 로그인 아이디로 사용합니다.
                .usernameParameter("loginId")

                // 로그인이 성공하면 페이지를 이동시키지 않고
                // React가 처리할 수 있도록 HTTP 200과 JSON을 반환합니다.
                .successHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");

                    response.getWriter().write(
                            "{\"message\":\"로그인되었습니다.\"}"
                    );
                })

                // 이메일이나 비밀번호가 틀리면 HTTP 401과 오류 메시지를 React에 반환합니다.
                .failureHandler((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");

                    response.getWriter().write(
                            "{\"message\":\"아이디 또는 비밀번호를 확인해주세요.\"}"
                    );
                })
        )

        .logout(logout -> logout

                // React가 로그아웃 요청을 전송할 주소입니다.
                .logoutUrl("/api/auth/logout")

                // 로그아웃이 완료되면 페이지를 이동시키지 않고
                // 처리 성공을 의미하는 HTTP 204를 반환합니다.
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                })
        )

        // ---------------- [소셜 로그인 설정 추가] ---------------------------
                .oauth2Login(oauthLogin -> oauthLogin

                        /*
                         * 소셜 로그인 성공 후
                         * 추가정보 입력 여부에 따라 이동할 화면을 결정합니다.
                         */
                        .successHandler((request, response, authentication) -> {

                            String identifier =
                                    authentication.getName();

                            Member member =
                                    memberService.findByAuthenticationIdentifier(
                                            identifier
                                    );

                            boolean additionalInfoRequired =
                                    member != null
                                            && (
                                            member.getBirthDate() == null
                                                    || member.getPhone() == null
                                                    || member.getPhone().isBlank()
                                                    || member.getEmail() == null
                                                    || member.getEmail().isBlank()
                                    );

                            if (additionalInfoRequired) {
                                response.sendRedirect(
                                        frontendUrl + "/additional-info"
                                );

                                return;
                            }

                            Object savedReturnTo =
                                    request.getSession()
                                            .getAttribute(
                                                    "OAUTH_RETURN_TO"
                                            );

                            String returnTo =
                                    savedReturnTo instanceof String value
                                            && value.startsWith("/")
                                            && !value.startsWith("//")
                                            ? value
                                            : "/";

                            request.getSession()
                                    .removeAttribute(
                                            "OAUTH_RETURN_TO"
                                    );

                            response.sendRedirect(
                                    frontendUrl + returnTo
                            );
                        })

                        .userInfoEndpoint(userInfoEndpointConfig ->
                                userInfoEndpointConfig
                                        .userService(customOAuth2UserService)
                        )

                        .failureHandler((request, response, exception) -> {

                            request.getSession()
                                    .removeAttribute(
                                            "OAUTH_RETURN_TO"
                                    );

                            String errorCode = "oauth_failed";

                            /*
                             * OAuth 처리 과정에서 예외가 다른 예외로 감싸질 수 있으므로
                             * cause 체인까지 확인합니다.
                             */
                            Throwable currentException = exception;

                            while (currentException != null) {

                                if (
                                        currentException
                                                instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauthException
                                ) {
                                    String oauthErrorCode =
                                            oauthException
                                                    .getError()
                                                    .getErrorCode();

                                    if ("duplicate_email".equals(oauthErrorCode)) {
                                        errorCode = "duplicate_email";
                                        break;
                                    }

                                    if ("duplicate_phone".equals(oauthErrorCode)) {
                                        errorCode = "duplicate_phone";
                                        break;
                                    }
                                }

                                /*
                                 * Spring Security가 OAuth 예외를 감싸면서
                                 * 원래 오류 코드가 message에 남는 경우도 대응합니다.
                                 */
                                String exceptionMessage =
                                        currentException.getMessage();

                                if (
                                        exceptionMessage != null
                                                && exceptionMessage.contains(
                                                "duplicate_email"
                                        )
                                ) {
                                    errorCode = "duplicate_email";
                                    break;
                                }

                                if (
                                        exceptionMessage != null
                                                && exceptionMessage.contains(
                                                "duplicate_phone"
                                        )
                                ) {
                                    errorCode = "duplicate_phone";
                                    break;
                                }

                                currentException =
                                        currentException.getCause();
                            }

                            response.sendRedirect(
                                    frontendUrl
                                            + "/login?oauthError="
                                            + errorCode
                            );
                        })
                );

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
