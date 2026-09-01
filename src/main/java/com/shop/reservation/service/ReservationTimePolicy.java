package com.shop.reservation.service;

import com.shop.center.entity.Center;
import com.shop.reservation.constant.DonationType;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class ReservationTimePolicy {

    private static final DateTimeFormatter
            TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");


    /**
     * 해당 헌혈 종류가 센터 운영 종료 전에
     * 실제 헌혈을 마칠 수 있는 예약 시작시간인지 확인합니다.
     *
     * 예)
     * 센터 종료 18:00
     * 전혈 30분  → 17:30 가능
     * 혈장 60분  → 17:00 가능
     * 혈소판 90분 → 16:30 가능
     */
    public boolean isReservableStartTime(
            Center center,
            LocalDate reservationDate,
            DonationType donationType,
            LocalTime startTime
    ) {

        if (
                center == null
                        || reservationDate == null
                        || donationType == null
                        || startTime == null
        ) {
            return false;
        }


        LocalTime closingTime =
                getClosingTime(
                        center,
                        reservationDate
                );


        if (closingTime == null) {
            return false;
        }


        LocalTime donationEndTime =
                startTime.plusMinutes(
                        donationType
                                .getEquipmentUsageMinutes()
                );


        /*
         * 종료시간과 정확히 같으면 허용합니다.
         *
         * 센터 18:00 종료 기준
         * 전혈 17:30 → 18:00 = 가능
         * 전혈 18:00 → 18:30 = 불가
         */
        return !donationEndTime.isAfter(
                closingTime
        );
    }


    /**
     * 해당 날짜의 센터 운영 종료시간을 반환합니다.
     *
     * 운영시간이 없으면 휴무로 보고 null을 반환합니다.
     */
    public LocalTime getClosingTime(
            Center center,
            LocalDate reservationDate
    ) {

        if (
                center == null
                        || reservationDate == null
        ) {
            return null;
        }


        String operatingHours =
                getOperatingHours(
                        center,
                        reservationDate.getDayOfWeek()
                );


        if (
                operatingHours == null
                        || operatingHours.isBlank()
                        || !operatingHours.contains("~")
        ) {
            return null;
        }


        String[] parts =
                operatingHours.split("~");


        if (parts.length != 2) {
            return null;
        }


        try {

            return LocalTime.parse(
                    parts[1].trim(),
                    TIME_FORMATTER
            );

        } catch (Exception e) {

            return null;
        }
    }


    private String getOperatingHours(
            Center center,
            DayOfWeek dayOfWeek
    ) {

        return switch (dayOfWeek) {

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
    }
}