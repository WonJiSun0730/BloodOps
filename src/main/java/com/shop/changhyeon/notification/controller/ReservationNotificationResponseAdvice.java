package com.shop.changhyeon.notification.controller;

import com.shop.changhyeon.notification.service.KakaoReservationNotificationService;
import com.shop.reservation.controller.ReservationApiController;
import com.shop.reservation.dto.AdminReservationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(assignableTypes = ReservationApiController.class)
@RequiredArgsConstructor
@Slf4j
public class ReservationNotificationResponseAdvice
        implements ResponseBodyAdvice<Object> {

    private final KakaoReservationNotificationService
            notificationService;

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>>
                    converterType
    ) {
        return "createReservation".equals(
                returnType.getMethod() != null
                        ? returnType.getMethod().getName()
                        : ""
        );
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>>
                    selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (
                body
                        instanceof AdminReservationResponseDto reservation
        ) {
            Authentication authentication =
                    SecurityContextHolder.getContext()
                            .getAuthentication();

            if (
                    authentication != null
                            && authentication.isAuthenticated()
            ) {
                try {
                    notificationService.sendAfterReservation(
                            reservation,
                            authentication
                    );
                } catch (RuntimeException exception) {
                    log.warn(
                            "예약은 완료되었지만 카카오 메시지 후처리에 실패했습니다. reservationId={}, reason={}",
                            reservation.getId(),
                            exception.getMessage()
                    );
                }
            }
        }

        return body;
    }
}
