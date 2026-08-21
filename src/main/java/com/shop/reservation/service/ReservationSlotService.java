package com.shop.reservation.service;

import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.dto.AdminReservationSlotCreateDto;
import com.shop.reservation.dto.AdminReservationSlotResponseDto;
import com.shop.reservation.entity.ReservationSlot;
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

        List<ReservationSlot> slots =
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
                throw new IllegalStateException(
                        "이미 존재하는 예약 슬롯이 있습니다: "
                                + currentTime
                );
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
                            .capacity(
                                    request.getCapacity()
                            )
                            .active(true)
                            .build();

            slots.add(slot);

            currentTime = slotEndTime;
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

        if (
                request.getCapacity() == null
                        && request.getActive() == null
        ) {
            throw new IllegalArgumentException(
                    "변경할 내용을 입력해주세요."
            );
        }

        if (request.getCapacity() != null) {
            if (request.getCapacity() <= 0) {
                throw new IllegalArgumentException(
                        "예약 정원은 1명 이상이어야 합니다."
                );
            }

            slot.setCapacity(
                    request.getCapacity()
            );
        }

        if (request.getActive() != null) {
            slot.setActive(
                    request.getActive()
            );
        }

        return toResponseDto(slot);
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

        if (request.getCapacity() <= 0) {
            throw new IllegalArgumentException(
                    "예약 정원은 1명 이상이어야 합니다."
            );
        }
    }

    private AdminReservationSlotResponseDto toResponseDto(
            ReservationSlot slot
    ) {
        long reservedCount =
                reservationRepository
                        .countBySlot_IdAndStatus(
                                slot.getId(),
                                ReservationStatus.RESERVED
                        );

        return new AdminReservationSlotResponseDto(
                slot,
                reservedCount
        );
    }
}