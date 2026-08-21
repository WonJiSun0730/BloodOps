package com.shop.reservation.controller;

import com.shop.reservation.constant.DonationType;
import com.shop.reservation.dto.AdminReservationSlotCreateDto;
import com.shop.reservation.dto.AdminReservationSlotResponseDto;
import com.shop.reservation.dto.AdminReservationSlotUpdateDto;
import com.shop.reservation.service.ReservationSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reservation-slots")
@RequiredArgsConstructor
public class AdminReservationSlotApiController {

    private final ReservationSlotService
            reservationSlotService;


    /*
     * 센터별 예약 슬롯을 일괄 생성합니다.
     *
     * POST /api/admin/reservation-slots
     */
    @PostMapping
    public ResponseEntity<?> createSlots(
            @RequestBody
            AdminReservationSlotCreateDto request
    ) {
        try {
            List<AdminReservationSlotResponseDto>
                    slots =
                    reservationSlotService
                            .createSlots(request);

            return ResponseEntity.ok(
                    Map.of(
                            "createdCount",
                            slots.size(),

                            "slots",
                            slots
                    )
            );

        } catch (
                IllegalArgumentException
                | IllegalStateException e
        ) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    @GetMapping
    public ResponseEntity<?> getSlots(
            @RequestParam Long centerId,
            @RequestParam LocalDate reservationDate,
            @RequestParam DonationType donationType
    ) {
        try {
            List<AdminReservationSlotResponseDto>
                    slots =
                    reservationSlotService.getSlots(
                            centerId,
                            reservationDate,
                            donationType
                    );

            return ResponseEntity.ok(slots);

        } catch (
                IllegalArgumentException
                | IllegalStateException e
        ) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PatchMapping("/{slotId}")
    public ResponseEntity<?> updateSlot(
            @PathVariable Long slotId,
            @RequestBody
            AdminReservationSlotUpdateDto request
    ) {
        try {
            AdminReservationSlotResponseDto slot =
                    reservationSlotService
                            .updateSlot(
                                    slotId,
                                    request
                            );

            return ResponseEntity.ok(slot);

        } catch (
                IllegalArgumentException
                | IllegalStateException e
        ) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}