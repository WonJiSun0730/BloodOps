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
}