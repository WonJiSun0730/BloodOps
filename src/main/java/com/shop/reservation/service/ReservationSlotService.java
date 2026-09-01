package com.shop.reservation.service;

import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.dto.*;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationSlotService {

    private final ReservationSlotRepository
            reservationSlotRepository;

    private final ReservationRepository
            reservationRepository;

    private final EquipmentGroupRepository equipmentGroupRepository;

    private final CenterRepository centerRepository;

    private final ReservationTimePolicy reservationTimePolicy;

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
                        && reservationTimePolicy
                        .isReservableStartTime(
                                center,
                                request.getReservationDate(),
                                request.getDonationType(),
                                currentTime
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

    public AdminReservationSlotBulkResultDto
    createBulkSlots(
            AdminReservationSlotBulkCreateDto request
    ) {

        validateBulkCreateRequest(request);


        Center center =
                centerRepository
                        .findById(
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


        int createdCount = 0;
        int duplicateSkippedCount = 0;
        int closedDateCount = 0;


        LocalDate currentDate =
                request.getStartDate();


        while (
                !currentDate.isAfter(
                        request.getEndDate()
                )
        ) {

            /*
             * 관리자가 특정 요일만 선택했다면
             * 선택되지 않은 요일은 그냥 건너뜁니다.
             */
            if (
                    request.getDaysOfWeek() != null
                            && !request.getDaysOfWeek().isEmpty()
                            && !request.getDaysOfWeek()
                            .contains(
                                    currentDate.getDayOfWeek()
                            )
            ) {

                currentDate =
                        currentDate.plusDays(1);

                continue;
            }


            TimeRange operatingRange =
                    resolveOperatingRange(
                            center,
                            currentDate,
                            request
                    );


            /*
             * 센터 운영시간 기준인데 해당 요일이 휴무이면
             * 날짜 자체를 건너뜁니다.
             */
            if (operatingRange == null) {

                closedDateCount++;

                currentDate =
                        currentDate.plusDays(1);

                continue;
            }


            for (
                    DonationType donationType
                    : request.getDonationTypes()
            ) {

                EquipmentGroup equipmentGroup =
                        equipmentGroupRepository
                                .findByCenter_IdAndGroupType(
                                        center.getId(),
                                        donationType
                                                .getEquipmentGroupType()
                                )
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                donationType
                                                        + " 헌혈에 사용할 장비그룹이 설정되지 않았습니다."
                                        )
                                );


                if (!equipmentGroup.isActive()) {
                    continue;
                }


                LocalTime currentTime =
                        operatingRange.start();


                while (
                        !currentTime
                                .plusMinutes(
                                        request.getIntervalMinutes()
                                )
                                .isAfter(
                                        operatingRange.end()
                                )
                                && reservationTimePolicy
                                .isReservableStartTime(
                                        center,
                                        currentDate,
                                        donationType,
                                        currentTime
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
                                            currentDate,
                                            currentTime,
                                            donationType
                                    );


                    if (duplicate) {

                        duplicateSkippedCount++;

                        currentTime =
                                slotEndTime;

                        continue;
                    }


                    ReservationSlot slot =
                            ReservationSlot.builder()
                                    .center(center)
                                    .reservationDate(
                                            currentDate
                                    )
                                    .startTime(
                                            currentTime
                                    )
                                    .endTime(
                                            slotEndTime
                                    )
                                    .donationType(
                                            donationType
                                    )
                                    .equipmentGroup(
                                            equipmentGroup
                                    )
                                    .capacity(
                                            equipmentGroup.getCapacity()
                                    )
                                    .active(true)
                                    .build();


                    reservationSlotRepository.save(
                            slot
                    );

                    createdCount++;

                    currentTime =
                            slotEndTime;
                }
            }


            currentDate =
                    currentDate.plusDays(1);
        }


        return AdminReservationSlotBulkResultDto
                .builder()
                .createdCount(
                        createdCount
                )
                .duplicateSkippedCount(
                        duplicateSkippedCount
                )
                .closedDateCount(
                        closedDateCount
                )
                .build();
    }

    public List<AdminReservationSlotResponseDto> getSlots(
            Long centerId,
            LocalDate reservationDate,
            DonationType donationType
    ) {
        return getSlots(
                centerId,
                reservationDate,
                donationType,
                null
        );
    }

    @Transactional(readOnly = true)
    public List<AdminReservationSlotResponseDto> getSlots(
            Long centerId,
            LocalDate reservationDate,
            DonationType donationType,
            Long excludeReservationId
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

        Center center =
                centerRepository
                        .findById(centerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "헌혈센터를 찾을 수 없습니다."
                                )
                        );


        return reservationSlotRepository
                .findByCenter_IdAndReservationDateAndDonationTypeOrderByStartTimeAsc(
                        centerId,
                        reservationDate,
                        donationType
                )
                .stream()

                /*
                 * 실제 헌혈 종료시간이
                 * 센터 운영 종료시간을 넘는 슬롯은
                 * 사용자에게 노출하지 않습니다.
                 */
                .filter(slot ->
                        reservationTimePolicy
                                .isReservableStartTime(
                                        center,
                                        reservationDate,
                                        donationType,
                                        slot.getStartTime()
                                )
                )

                .map(slot ->
                        toResponseDto(
                                slot,
                                excludeReservationId
                        )
                )
                .toList();
    }

    public Set<LocalDate> getAvailableDates(
            Long centerId,
            int year,
            int month
    ) {
        return getAvailableDates(
                centerId,
                year,
                month,
                null
        );
    }

    @Transactional(readOnly = true)
    public Set<LocalDate> getAvailableDates(
            Long centerId,
            int year,
            int month,
            Long excludeReservationId
    ) {
        if (centerId == null) {
            throw new IllegalArgumentException(
                    "헌혈센터를 선택해주세요."
            );
        }

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "조회 월이 올바르지 않습니다."
            );
        }


        LocalDate monthStart =
                LocalDate.of(
                        year,
                        month,
                        1
                );

        LocalDate monthEnd =
                monthStart.withDayOfMonth(
                        monthStart.lengthOfMonth()
                );


        LocalDate tomorrow =
                LocalDate.now().plusDays(1);

        LocalDate reservationLimit =
                LocalDate.now().plusMonths(2);


        LocalDate searchStart =
                monthStart.isBefore(tomorrow)
                        ? tomorrow
                        : monthStart;

        LocalDate searchEnd =
                monthEnd.isAfter(reservationLimit)
                        ? reservationLimit
                        : monthEnd;


        if (searchStart.isAfter(searchEnd)) {
            return Set.of();
        }


        /*
         * 비활성 슬롯은 DB 조회 단계에서 제외합니다.
         */
        List<ReservationSlot> slots =
                reservationSlotRepository
                        .findByCenter_IdAndReservationDateBetweenAndActiveTrueOrderByReservationDateAscStartTimeAsc(
                                centerId,
                                searchStart,
                                searchEnd
                        );


        Set<LocalDate> availableDates =
                new LinkedHashSet<>();


        /*
         * 같은 장비그룹 + 날짜의 예약목록을
         * 슬롯마다 다시 SELECT하지 않도록 캐시합니다.
         */
        java.util.Map<String, List<Reservation>>
                reservationCache =
                new java.util.HashMap<>();


        for (ReservationSlot slot : slots) {

            /*
             * 헌혈 종류별 접수 마감시간 이후 슬롯은
             * 예약 가능한 날짜 판정에서도 제외합니다.
             */
            if (
                    !reservationTimePolicy
                            .isReservableStartTime(
                                    slot.getCenter(),
                                    slot.getReservationDate(),
                                    slot.getDonationType(),
                                    slot.getStartTime()
                            )
            ) {
                continue;
            }

            /*
             * 해당 날짜가 이미 예약 가능하다고 판정됐다면
             * 같은 날짜의 나머지 슬롯은 계산할 필요가 없습니다.
             */
            if (
                    availableDates.contains(
                            slot.getReservationDate()
                    )
            ) {
                continue;
            }


            EquipmentGroup equipmentGroup =
                    slot.getEquipmentGroup();


            if (equipmentGroup == null) {

                long reservedCount =
                        countLegacySlotReservations(
                                slot,
                                excludeReservationId
                        );

                if (
                        slot.getCapacity()
                                - reservedCount
                                > 0
                ) {
                    availableDates.add(
                            slot.getReservationDate()
                    );
                }

                continue;
            }


            if (!equipmentGroup.isActive()) {
                continue;
            }


            String cacheKey =
                    equipmentGroup.getId()
                            + ":"
                            + slot.getReservationDate();


            List<Reservation> groupReservations =
                    reservationCache.computeIfAbsent(
                            cacheKey,
                            ignored ->
                                    reservationRepository
                                            .findEquipmentGroupReservations(
                                                    equipmentGroup.getId(),
                                                    slot.getReservationDate(),
                                                    ReservationStatus.RESERVED
                                            )
                    );


            LocalTime targetStart =
                    slot.getStartTime();

            LocalTime targetEnd =
                    targetStart.plusMinutes(
                            slot.getDonationType()
                                    .getEquipmentUsageMinutes()
                    );


            long concurrentUsage =
                    EquipmentCapacityCalculator
                            .calculateMaxConcurrentUsage(
                                    groupReservations,
                                    targetStart,
                                    targetEnd,
                                    excludeReservationId
                            );


            if (
                    concurrentUsage
                            < equipmentGroup.getCapacity()
            ) {
                availableDates.add(
                        slot.getReservationDate()
                );
            }
        }


        return availableDates;
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

    private TimeRange resolveOperatingRange(
            Center center,
            LocalDate date,
            AdminReservationSlotBulkCreateDto request
    ) {

        String hours =
                switch (date.getDayOfWeek()) {

                    case MONDAY,
                         TUESDAY,
                         WEDNESDAY,
                         THURSDAY,
                         FRIDAY ->
                            center.getWeekdayHours();

                    case SATURDAY ->
                            center.getSaturdayHours();

                    case SUNDAY ->
                            center.getSundayHours();
                };


        /*
         * 해당 요일의 운영시간이 없으면 휴무입니다.
         * 직접 설정을 사용해도 휴무일에는 생성하지 않습니다.
         */
        if (
                hours == null
                        || hours.isBlank()
        ) {
            return null;
        }


        TimeRange centerRange =
                parseCenterHours(hours);


        /*
         * 센터 운영시간 자동 사용
         */
        if (request.isUseCenterHours()) {
            return centerRange;
        }


        /*
         * 직접 설정 사용
         */
        LocalTime customStart =
                request.getCustomStartTime();

        LocalTime customEnd =
                request.getCustomEndTime();


        /*
         * 직접 설정하더라도
         * 센터 실제 운영시간 밖으로 나갈 수 없습니다.
         */
        if (
                customStart.isBefore(
                        centerRange.start()
                )
                        || customEnd.isAfter(
                        centerRange.end()
                )
        ) {
            throw new IllegalArgumentException(
                    "직접 설정 시간은 센터 운영시간 안에서만 지정할 수 있습니다."
            );
        }


        return new TimeRange(
                customStart,
                customEnd
        );
    }


    private TimeRange parseCenterHours(
            String hours
    ) {

        if (
                hours == null
                        || hours.isBlank()
                        || !hours.contains("~")
        ) {
            throw new IllegalStateException(
                    "센터 운영시간 형식이 올바르지 않습니다."
            );
        }


        String[] parts =
                hours.split("~");


        if (parts.length != 2) {
            throw new IllegalStateException(
                    "센터 운영시간 형식이 올바르지 않습니다."
            );
        }


        try {

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "HH:mm"
                    );


            LocalTime start =
                    LocalTime.parse(
                            parts[0].trim(),
                            formatter
                    );


            LocalTime end =
                    LocalTime.parse(
                            parts[1].trim(),
                            formatter
                    );


            if (!start.isBefore(end)) {
                throw new IllegalStateException(
                        "센터 운영 종료 시간은 시작 시간보다 늦어야 합니다."
                );
            }


            return new TimeRange(
                    start,
                    end
            );

        } catch (Exception e) {

            if (e instanceof IllegalStateException) {
                throw e;
            }


            throw new IllegalStateException(
                    "센터 운영시간 형식이 올바르지 않습니다.",
                    e
            );
        }
    }


    private record TimeRange(
            LocalTime start,
            LocalTime end
    ) {
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

    private void validateBulkCreateRequest(
            AdminReservationSlotBulkCreateDto request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "예약 슬롯 생성 정보를 입력해주세요."
            );
        }


        if (request.getCenterId() == null) {
            throw new IllegalArgumentException(
                    "헌혈센터를 선택해주세요."
            );
        }


        if (
                request.getStartDate() == null
                        || request.getEndDate() == null
        ) {
            throw new IllegalArgumentException(
                    "생성 시작일과 종료일을 선택해주세요."
            );
        }


        if (
                request.getStartDate()
                        .isAfter(
                                request.getEndDate()
                        )
        ) {
            throw new IllegalArgumentException(
                    "종료일은 시작일보다 빠를 수 없습니다."
            );
        }


        if (
                request.getStartDate()
                        .isBefore(
                                LocalDate.now()
                        )
        ) {
            throw new IllegalArgumentException(
                    "과거 날짜에는 예약 슬롯을 생성할 수 없습니다."
            );
        }

        if (
                request.getDonationTypes() == null
                        || request.getDonationTypes().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "헌혈 종류를 하나 이상 선택해주세요."
            );
        }


        if (request.getIntervalMinutes() <= 0) {
            throw new IllegalArgumentException(
                    "슬롯 간격은 1분 이상이어야 합니다."
            );
        }


        if (!request.isUseCenterHours()) {

            if (
                    request.getCustomStartTime() == null
                            || request.getCustomEndTime() == null
            ) {
                throw new IllegalArgumentException(
                        "직접 설정할 시작 시간과 종료 시간을 입력해주세요."
                );
            }


            if (
                    !request.getCustomStartTime()
                            .isBefore(
                                    request.getCustomEndTime()
                            )
            ) {
                throw new IllegalArgumentException(
                        "종료 시간은 시작 시간보다 늦어야 합니다."
                );
            }
        }
    }

    private long countLegacySlotReservations(
            ReservationSlot slot,
            Long excludeReservationId
    ) {
        long reservedCount =
                reservationRepository
                        .countBySlot_IdAndStatus(
                                slot.getId(),
                                ReservationStatus.RESERVED
                        );

        if (
                excludeReservationId == null
                        || reservedCount <= 0
        ) {
            return reservedCount;
        }

        return reservationRepository
                .findById(excludeReservationId)
                .filter(reservation ->
                        reservation.getStatus()
                                == ReservationStatus.RESERVED
                )
                .filter(reservation ->
                        reservation.getSlot() != null
                                && reservation.getSlot()
                                .getId()
                                .equals(slot.getId())
                )
                .map(reservation ->
                        Math.max(
                                reservedCount - 1,
                                0
                        )
                )
                .orElse(reservedCount);
    }

    private AdminReservationSlotResponseDto toResponseDto(
            ReservationSlot slot
    ) {
        return toResponseDto(
                slot,
                null
        );
    }

    private AdminReservationSlotResponseDto toResponseDto(
            ReservationSlot slot,
            Long excludeReservationId
    ) {
        boolean hasReservationHistory =
                reservationRepository
                        .existsBySlot_Id(
                                slot.getId()
                        );

        EquipmentGroup equipmentGroup =
                slot.getEquipmentGroup();

        if (equipmentGroup == null) {

            long reservedCount =
                    countLegacySlotReservations(
                            slot,
                            excludeReservationId
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
                                excludeReservationId
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