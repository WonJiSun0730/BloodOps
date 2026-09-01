package com.shop.reservation.exception;

import com.shop.reservation.dto.AdminReservationResponseDto;
import lombok.Getter;

@Getter
public class ActiveReservationExistsException
        extends RuntimeException {

    private final AdminReservationResponseDto
            existingReservation;

    public ActiveReservationExistsException(
            AdminReservationResponseDto existingReservation
    ) {
        super(
                "진행 중인 헌혈 예약이 있습니다. "
                        + "새로 예약하면 기존 예약은 자동 취소됩니다."
        );

        this.existingReservation =
                existingReservation;
    }
}