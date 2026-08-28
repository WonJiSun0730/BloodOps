package com.shop.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationCreateDto {

    private Long slotId;

    private String donorName;

    private String residentRegistrationNumber;

    private String phone;

    private boolean privacyConsented;

    /*
     * 이미 진행 중인 예약이 있을 때
     * 기존 예약을 취소하고 새 예약으로 교체할지 여부
     */
    private boolean replaceExistingReservation;
}