package com.shop.reservation.dto;

import com.shop.reservation.constant.DonationType;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
public class AdminReservationSlotBulkCreateDto {

    private Long centerId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Set<DonationType> donationTypes;

    /*
     * 비어 있으면 기간 내 모든 요일을 대상으로 합니다.
     *
     * 프론트에서 월~금 / 토 / 일 선택 UI를 만들 때 사용합니다.
     */
    private Set<DayOfWeek> daysOfWeek;

    /*
     * true:
     * 센터 관리에 저장된 요일별 운영시간 사용
     *
     * false:
     * customStartTime / customEndTime 사용
     */
    private boolean useCenterHours = true;

    private LocalTime customStartTime;

    private LocalTime customEndTime;

    private int intervalMinutes = 30;
}