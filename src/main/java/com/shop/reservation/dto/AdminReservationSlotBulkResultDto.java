package com.shop.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReservationSlotBulkResultDto {

    private int createdCount;

    /*
     * 이미 같은 슬롯이 존재해서 생성하지 않은 개수
     */
    private int duplicateSkippedCount;

    /*
     * 휴무일 또는 운영시간이 없는 날짜로 인해
     * 생성하지 않은 날짜 수
     */
    private int closedDateCount;
}