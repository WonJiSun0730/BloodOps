package com.shop.reservation.service;

import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.dto.AdminReservationSlotCreateDto;
import com.shop.reservation.dto.AdminReservationSlotResponseDto;
import com.shop.reservation.entity.EquipmentGroup;
import com.shop.reservation.entity.Reservation;
import com.shop.reservation.entity.ReservationSlot;
import com.shop.reservation.exception.DuplicateReservationSlotException;
import com.shop.reservation.repository.EquipmentGroupRepository;
import com.shop.reservation.repository.ReservationRepository;
import com.shop.reservation.repository.ReservationSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shop.reservation.constant.DonationType;
import com.shop.reservation.dto.AdminReservationSlotUpdateDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationSlotService {

    private final ReservationSlotRepository
            reservationSlotRepository;

    private final ReservationRepository
            reservationRepository;

    private final EquipmentGroupRepository
            equipmentGroupRepository;

    private final CenterRepository centerRepository;


    /*
     * ADMIN이 지정한 시간 범위를 일정 간격으로 나누어
     * 예약 슬롯을 일괄 생성합니다.
     */
    public List<AdminReservationSlotResponseDto>
    createSlots(
            AdminReservationSlotCreateDto request
    ) {
        validateCreateRequest(request);

        Center center =
                centerRepository.findById(
                                request.getCenterId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "헌혈센터를 찾을 수 없습니다."
                                )
                        );

        if (!center.isActive()) {
            throw new IllegalStateException(
                    "운영 중인 센터에만 예약 슬롯을 생성할 수 있습니다."
            );
        }

        EquipmentGroup equipmentGroup =
                equipmentGroupRepository
                        .findByCenter_IdAndGroupType(
                                center.getId(),
                                request.getDonationType()
                                        .getEquipmentGroupType()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "해당 헌혈 종류에 사용할 장비그룹이 설정되지 않았습니다."
                                )
                        );

        if (!equipmentGroup.isActive()) {
            throw new IllegalStateException(
                    "현재 사용할 수 없는 장비그룹입니다."
            );
        }

        List<ReservationSlot> slots =
                new ArrayList<>();

        List<String> duplicateTimes =
                new ArrayList<>();

        LocalTime currentTime =
                request.getStartTime();

        while (
                !currentTime
                        .plusMinutes(
                                request.getIntervalMinutes()
                        )
                        .isAfter(
                                request.getEndTime()
                        )
        ) {
            LocalTime slotEndTime =
                    currentTime.plusMinutes(
                            request.getIntervalMinutes()
                    );

            boolean duplicate =
                    reservationSlotRepository
                            .existsByCenter_IdAndReservationDateAndStartTimeAndDonationType(
                                    center.getId(),
                                    request.getReservationDate(),
                                    currentTime,
                                    request.getDonationType()
                            );

            if (duplicate) {
                duplicateTimes.add(
                        currentTime.toString()
                );

                currentTime =
                        slotEndTime;

                continue;
            }

            ReservationSlot slot =
                    ReservationSlot.builder()
                            .center(center)
                            .reservationDate(
                                    request.getReservationDate()
                            )
                            .startTime(currentTime)
                            .endTime(slotEndTime)
                            .donationType(
                                    request.getDonationType()
                            )
                            .equipmentGroup(
                                    equipmentGroup
                            )
                            .capacity(
                                    equipmentGroup.getCapacity()
                            )
                            .active(true)
                            .build();

            slots.add(slot);

            currentTime = slotEndTime;
        }

        if (!duplicateTimes.isEmpty()) {
            throw new DuplicateReservationSlotException(
                    duplicateTimes
            );
        }

        return reservationSlotRepository
                .saveAll(slots)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminReservationSlotResponseDto> getSlots(
            Long centerId,
            LocalDate reservationDate,
            DonationType donationType
    ) {
        if (centerId == null) {
            throw new IllegalArgumentException(
                    "헌혈센터를 선택해주세요."
            );
        }

        if (reservationDate == null) {
            throw new IllegalArgumentException(
                    "예약 날짜를 선택해주세요."
            );
        }

        if (donationType == null) {
            throw new IllegalArgumentException(
                    "헌혈 종류를 선택해주세요."
            );
        }

        return reservationSlotRepository
                .findByCenter_IdAndReservationDateAndDonationTypeOrderByStartTimeAsc(
                        centerId,
                        reservationDate,
                        donationType
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }


    public AdminReservationSlotResponseDto updateSlot(
            Long slotId,
            AdminReservationSlotUpdateDto request
    ) {
        ReservationSlot slot =
                reservationSlotRepository
                        .findById(slotId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "예약 슬롯을 찾을 수 없습니다."
                                )
                        );

        if (request.getActive() == null) {
            throw new IllegalArgumentException(
                    "변경할 내용을 입력해주세요."
            );
        }

        if (request.getActive() != null) {
            slot.setActive(
                    request.getActive()
            );
        }

        return toResponseDto(slot);
    }

    /*
     * 예약 이력이 전혀 없는 슬롯만 삭제합니다.
     *
     * 취소된 예약도 운영 이력이므로
     * CANCELED 예약이 존재하면 삭제하지 않습니다.
     */
    public void deleteSlot(
            Long slotId
    ) {

        if (slotId == null) {
            throw new IllegalArgumentException(
                    "삭제할 예약 슬롯을 선택해주세요."
            );
        }

        ReservationSlot slot =
                reservationSlotRepository
                        .findById(slotId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "예약 슬롯을 찾을 수 없습니다."
                                )
                        );

        boolean hasReservationHistory =
                reservationRepository
                        .existsBySlot_Id(
                                slot.getId()
                        );

        if (hasReservationHistory) {
            throw new IllegalStateException(
                    "예약 이력이 있는 슬롯은 삭제할 수 없습니다."
            );
        }

        reservationSlotRepository.delete(
                slot
        );
    }


    private void validateCreateRequest(
            AdminReservationSlotCreateDto request
    ) {
        if (request.getCenterId() == null) {
            throw new IllegalArgumentException(
                    "헌혈센터를 선택해주세요."
            );
        }

        if (request.getReservationDate() == null) {
            throw new IllegalArgumentException(
                    "예약 날짜를 선택해주세요."
            );
        }

        if (
                request.getReservationDate()
                        .isBefore(LocalDate.now())
        ) {
            throw new IllegalArgumentException(
                    "과거 날짜에는 예약 슬롯을 생성할 수 없습니다."
            );
        }

        if (request.getDonationType() == null) {
            throw new IllegalArgumentException(
                    "헌혈 종류를 선택해주세요."
            );
        }

        if (
                request.getStartTime() == null
                        || request.getEndTime() == null
        ) {
            throw new IllegalArgumentException(
                    "시작 시간과 종료 시간을 입력해주세요."
            );
        }

        if (
                !request.getStartTime()
                        .isBefore(request.getEndTime())
        ) {
            throw new IllegalArgumentException(
                    "종료 시간은 시작 시간보다 늦어야 합니다."
            );
        }

        if (request.getIntervalMinutes() <= 0) {
            throw new IllegalArgumentException(
                    "슬롯 간격은 1분 이상이어야 합니다."
            );
        }

    }

    private AdminReservationSlotResponseDto toResponseDto(
            ReservationSlot slot
    ) {
        boolean hasReservationHistory =
                reservationRepository
                        .existsBySlot_Id(
                                slot.getId()
                        );

        EquipmentGroup equipmentGroup =
                slot.getEquipmentGroup();

        /*
         * 장비그룹 구조 적용 전에 생성된 기존 슬롯은
         * equipment_group_id가 없을 수 있으므로
         * 기존 슬롯 정원 방식으로 임시 응답합니다.
         */
        if (equipmentGroup == null) {

            long reservedCount =
                    reservationRepository
                            .countBySlot_IdAndStatus(
                                    slot.getId(),
                                    ReservationStatus.RESERVED
                            );

            long remainingCapacity =
                    Math.max(
                            slot.getCapacity()
                                    - reservedCount,
                            0
                    );

            return new AdminReservationSlotResponseDto(
                    slot,
                    slot.getCapacity(),
                    reservedCount,
                    remainingCapacity,
                    hasReservationHistory
            );
        }


        LocalTime targetStart =
                slot.getStartTime();

        LocalTime targetEnd =
                targetStart.plusMinutes(
                        slot.getDonationType()
                                .getEquipmentUsageMinutes()
                );


        List<Reservation> groupReservations =
                reservationRepository
                        .findEquipmentGroupReservations(
                                equipmentGroup.getId(),
                                slot.getReservationDate(),
                                ReservationStatus.RESERVED
                        );


        long concurrentUsage =
                EquipmentCapacityCalculator
                        .calculateMaxConcurrentUsage(
                                groupReservations,
                                targetStart,
                                targetEnd,
                                null
                        );


        if (!equipmentGroup.isActive()) {
            return new AdminReservationSlotResponseDto(
                    slot,
                    equipmentGroup.getCapacity(),
                    concurrentUsage,
                    0,
                    hasReservationHistory
            );
        }


        long remainingCapacity =
                Math.max(
                        equipmentGroup.getCapacity()
                                - concurrentUsage,
                        0
                );


        return new AdminReservationSlotResponseDto(
                slot,
                equipmentGroup.getCapacity(),
                concurrentUsage,
                remainingCapacity,
                hasReservationHistory
        );
    }
}