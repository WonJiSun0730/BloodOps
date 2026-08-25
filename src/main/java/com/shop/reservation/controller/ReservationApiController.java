package com.shop.reservation.controller;

import com.shop.reservation.constant.DonationType;
import com.shop.reservation.dto.AdminReservationResponseDto;
import com.shop.reservation.dto.AdminReservationSlotChangeDto;
import com.shop.reservation.dto.AdminReservationSlotResponseDto;
import com.shop.reservation.dto.ReservationCreateDto;
import com.shop.reservation.service.ReservationService;
import com.shop.reservation.service.ReservationSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService
            reservationService;

    private final ReservationSlotService
            reservationSlotService;

    /*
     * 사용자가 예약 가능한 시간 슬롯 조회
     *
     * GET /api/reservations/slots
     *
     * 예:
     * /api/reservations/slots
     * ?centerId=1
     * &reservationDate=2026-08-28
     * &donationType=WHOLE_BLOOD
     */
    @GetMapping("/slots")
    public List<AdminReservationSlotResponseDto>
    getAvailableSlots(
            @RequestParam Long centerId,
            @RequestParam LocalDate reservationDate,
            @RequestParam DonationType donationType
    ) {
        return reservationSlotService
                .getSlots(
                        centerId,
                        reservationDate,
                        donationType
                )
                .stream()
                .filter(AdminReservationSlotResponseDto::isActive)
                .filter(slot ->
                        slot.getRemainingCapacity() > 0
                )
                .toList();
    }

    /*
     * 로그인한 회원의 예약 내역 조회
     *
     * GET /api/reservations
     */
    @GetMapping
    public List<AdminReservationResponseDto> getMyReservations(
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

        return reservationService
                .getMyReservations(
                        authentication.getName()
                );
    }

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

    /*
     * 로그인한 회원 본인의 예약 취소
     *
     * PATCH /api/reservations/{reservationId}/cancel
     */
    @PatchMapping("/{reservationId}/cancel")
    public AdminReservationResponseDto cancelMyReservation(
            @PathVariable Long reservationId,
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

        return reservationService
                .cancelMyReservation(
                        reservationId,
                        authentication.getName()
                );
    }

    /*
     * 로그인한 회원 본인의 예약 시간 변경
     *
     * PATCH /api/reservations/{reservationId}/slot
     */
    @PatchMapping("/{reservationId}/slot")
    public AdminReservationResponseDto changeMyReservationSlot(
            @PathVariable Long reservationId,
            @RequestBody AdminReservationSlotChangeDto request,
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

        return reservationService
                .changeMyReservationSlot(
                        reservationId,
                        request.getSlotId(),
                        authentication.getName()
                );
    }
}