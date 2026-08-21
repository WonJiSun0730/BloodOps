package com.shop.reservation.controller;

import com.shop.reservation.dto.AdminReservationResponseDto;
import com.shop.reservation.dto.ReservationCreateDto;
import com.shop.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService
            reservationService;


    /*
     * 로그인한 회원의 예약 생성
     *
     * POST /api/reservations
     */
    @PostMapping
    public AdminReservationResponseDto createReservation(
            @RequestBody ReservationCreateDto request,
            Authentication authentication
    ) {
        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {
            throw new IllegalStateException(
                    "로그인이 필요합니다."
            );
        }

        return reservationService.createReservation(
                request.getSlotId(),
                authentication.getName()
        );
    }
}