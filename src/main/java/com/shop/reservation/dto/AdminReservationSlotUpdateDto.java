package com.shop.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReservationSlotUpdateDto {

    private Integer capacity;

    private Boolean active;
}