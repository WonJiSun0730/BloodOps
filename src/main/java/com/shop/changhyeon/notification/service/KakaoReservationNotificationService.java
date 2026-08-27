package com.shop.changhyeon.notification.service;

import com.shop.changhyeon.notification.entity.KakaoMessageTemplate;
import com.shop.member.constant.LoginProvider;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import com.shop.reservation.dto.AdminReservationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoReservationNotificationService {

    private final MemberRepository memberRepository;
    private final KakaoMessageTemplateService templateService;
    private final KakaoMessageRenderer renderer;
    private final KakaoMessageApiClient apiClient;
    private final ObjectProvider<OAuth2AuthorizedClientService>
            authorizedClientServiceProvider;

    @Value("${kakao.message.enabled:false}")
    private boolean featureEnabled;

    public void sendAfterReservation(
            AdminReservationResponseDto reservation,
            Authentication authentication
    ) {
        if (!featureEnabled) {
            log.debug(
                    "카카오 예약 메시지 기능이 비활성화되어 있습니다. reservationId={}",
                    reservation.getId()
            );
            return;
        }

        Member member =
                memberRepository.findByEmail(
                        reservation.getMemberEmail()
                );

        if (
                member == null
                        || member.getProvider()
                        != LoginProvider.KAKAO
        ) {
            return;
        }

        KakaoMessageTemplate template =
                templateService.getTemplateEntity();

        if (!template.isEnabled()) {
            return;
        }

        OAuth2AuthorizedClientService clientService =
                authorizedClientServiceProvider
                        .getIfAvailable();

        if (clientService == null) {
            log.warn(
                    "카카오 OAuth 클라이언트 저장소를 찾지 못했습니다. reservationId={}",
                    reservation.getId()
            );
            return;
        }

        OAuth2AuthorizedClient authorizedClient =
                clientService.loadAuthorizedClient(
                        "kakao",
                        authentication.getName()
                );

        if (authorizedClient == null) {
            log.warn(
                    "카카오 액세스 토큰을 찾지 못했습니다. reservationId={}",
                    reservation.getId()
            );
            return;
        }

        Instant expiresAt =
                authorizedClient.getAccessToken()
                        .getExpiresAt();

        if (
                expiresAt != null
                        && expiresAt.isBefore(Instant.now())
        ) {
            log.warn(
                    "카카오 액세스 토큰이 만료되었습니다. reservationId={}",
                    reservation.getId()
            );
            return;
        }

        try {
            String message = renderer.render(
                    template.getMessageText(),
                    reservation
            );

            renderer.validateLength(message);

            apiClient.send(
                    authorizedClient.getAccessToken()
                            .getTokenValue(),
                    message
            );

            log.info(
                    "카카오 예약 메시지 발송 성공. reservationId={}",
                    reservation.getId()
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "카카오 예약 메시지 발송 실패. reservationId={}, reason={}",
                    reservation.getId(),
                    exception.getMessage()
            );
        }
    }
}
