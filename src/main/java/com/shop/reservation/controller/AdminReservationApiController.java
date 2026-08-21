package com.shop.reservation.controller;

import com.shop.reservation.dto.AdminReservationResponseDto;
import com.shop.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationApiController {

    private final ReservationService
            reservationService;


    /*
     * ADMIN 전체 예약 조회
     *
     * GET /api/admin/reservations
     */
    @GetMapping
    public List<AdminReservationResponseDto>
    getReservations() {

        return reservationService
                .getAdminReservations();
    }

    /*
     * ADMIN 예약 취소
     *
     * PATCH /api/admin/reservations/{reservationId}/cancel
     */
    @PatchMapping("/{reservationId}/cancel")
    public AdminReservationResponseDto
    cancelReservation(
            @PathVariable Long reservationId
    ) {
        return reservationService
                .cancelReservation(
                        reservationId
                );
    }
}