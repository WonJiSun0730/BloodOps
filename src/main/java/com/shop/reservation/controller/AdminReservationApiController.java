package com.shop.reservation.controller;

import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.dto.AdminReservationResponseDto;
import com.shop.reservation.dto.AdminReservationSlotChangeDto;
import com.shop.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationApiController {

    private final ReservationService
            reservationService;


    @GetMapping
    public Page<AdminReservationResponseDto>
    getReservations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            ReservationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );

        return reservationService
                .getAdminReservations(
                        keyword,
                        status,
                        pageable
                );
    }

    /*
     * ADMIN 예약 상세 조회
     *
     * GET /api/admin/reservations/{reservationId}
     */
    @GetMapping("/{reservationId}")
    public AdminReservationResponseDto
    getReservation(
            @PathVariable Long reservationId
    ) {
        return reservationService
                .getAdminReservation(
                        reservationId
                );
    }

    /*
     * ADMIN 예약 슬롯 변경
     *
     * PATCH /api/admin/reservations/{reservationId}/slot
     */
    @PatchMapping("/{reservationId}/slot")
    public AdminReservationResponseDto
    changeReservationSlot(
            @PathVariable Long reservationId,
            @RequestBody
            AdminReservationSlotChangeDto request
    ) {
        return reservationService
                .changeReservationSlot(
                        reservationId,
                        request.getSlotId()
                );
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