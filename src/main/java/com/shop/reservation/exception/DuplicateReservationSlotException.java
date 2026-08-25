package com.shop.reservation.exception;

import java.util.List;

public class DuplicateReservationSlotException
        extends RuntimeException {

    private final List<String> duplicateTimes;

    public DuplicateReservationSlotException(
            List<String> duplicateTimes
    ) {
        super("이미 존재하는 예약 슬롯이 있습니다.");

        this.duplicateTimes =
                duplicateTimes;
    }

    public List<String> getDuplicateTimes() {
        return duplicateTimes;
    }
}