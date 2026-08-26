package com.shop.reservation.controller;

import com.shop.reservation.constant.DonationType;
import com.shop.reservation.dto.*;
import com.shop.reservation.exception.DuplicateReservationSlotException;
import com.shop.reservation.service.EquipmentGroupService;
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

    private final EquipmentGroupService
            equipmentGroupService;

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
                DuplicateReservationSlotException e
        ) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage(),

                                    "duplicateTimes",
                                    e.getDuplicateTimes()
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

    /*
     * 특정 센터의 공유 장비그룹을 조회합니다.
     *
     * GET
     * /api/admin/reservation-slots/equipment-groups?centerId=1
     */
    @GetMapping("/equipment-groups")
    public ResponseEntity<?> getEquipmentGroups(
            @RequestParam Long centerId
    ) {
        try {
            List<AdminEquipmentGroupResponseDto>
                    equipmentGroups =
                    equipmentGroupService
                            .getEquipmentGroups(
                                    centerId
                            );

            return ResponseEntity.ok(
                    equipmentGroups
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

    /*
     * 공유 장비그룹의 장비 수량 또는
     * 운영 여부를 수정합니다.
     *
     * PATCH
     * /api/admin/reservation-slots/equipment-groups/{equipmentGroupId}
     */
    @PatchMapping(
            "/equipment-groups/{equipmentGroupId}"
    )
    public ResponseEntity<?> updateEquipmentGroup(
            @PathVariable Long equipmentGroupId,

            @RequestBody
            AdminEquipmentGroupUpdateDto request
    ) {
        try {
            AdminEquipmentGroupResponseDto
                    equipmentGroup =
                    equipmentGroupService
                            .updateEquipmentGroup(
                                    equipmentGroupId,
                                    request
                            );

            return ResponseEntity.ok(
                    equipmentGroup
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

    /*
     * 예약 이력이 없는 슬롯을 삭제합니다.
     *
     * DELETE
     * /api/admin/reservation-slots/{slotId}
     */
    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> deleteSlot(
            @PathVariable Long slotId
    ) {
        try {
            reservationSlotService
                    .deleteSlot(
                            slotId
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "예약 슬롯이 삭제되었습니다.",
                            "slotId",
                            slotId
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
}