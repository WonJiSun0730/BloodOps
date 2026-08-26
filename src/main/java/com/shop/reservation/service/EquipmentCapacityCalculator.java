package com.shop.reservation.service;

import com.shop.reservation.entity.Reservation;
import com.shop.reservation.entity.ReservationSlot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

final class EquipmentCapacityCalculator {

    private EquipmentCapacityCalculator() {
    }


    /*
     * 대상 장비 사용 구간 안에서
     * 동시에 사용 중인 장비의 최대 개수를 계산합니다.
     *
     * 시간 구간은 [시작, 종료) 방식으로 처리합니다.
     *
     * 예:
     * 10:00 ~ 10:30
     * 10:30 ~ 11:00
     *
     * 두 예약은 서로 겹치지 않습니다.
     */
    static long calculateMaxConcurrentUsage(
            List<Reservation> reservations,
            LocalTime targetStart,
            LocalTime targetEnd,
            Long excludeReservationId
    ) {

        List<Reservation> candidates =
                reservations.stream()
                        .filter(reservation ->
                                excludeReservationId == null
                                        || !reservation
                                        .getId()
                                        .equals(
                                                excludeReservationId
                                        )
                        )
                        .filter(reservation -> {

                            ReservationSlot slot =
                                    reservation.getSlot();

                            LocalTime start =
                                    slot.getStartTime();

                            LocalTime end =
                                    start.plusMinutes(
                                            slot
                                                    .getDonationType()
                                                    .getEquipmentUsageMinutes()
                                    );

                            return start.isBefore(targetEnd)
                                    && end.isAfter(targetStart);
                        })
                        .toList();


        /*
         * 동시 사용량이 증가할 수 있는 시점은
         * 대상 구간 시작점 또는
         * 기존 예약의 시작 시점입니다.
         */
        List<LocalTime> checkTimes =
                new ArrayList<>();

        checkTimes.add(targetStart);

        for (Reservation reservation : candidates) {

            LocalTime start =
                    reservation
                            .getSlot()
                            .getStartTime();

            if (
                    !start.isBefore(targetStart)
                            && start.isBefore(targetEnd)
            ) {
                checkTimes.add(start);
            }
        }


        long maxConcurrentUsage = 0;

        for (LocalTime checkTime : checkTimes) {

            long concurrentUsage =
                    candidates.stream()
                            .filter(reservation -> {

                                ReservationSlot slot =
                                        reservation.getSlot();

                                LocalTime start =
                                        slot.getStartTime();

                                LocalTime end =
                                        start.plusMinutes(
                                                slot
                                                        .getDonationType()
                                                        .getEquipmentUsageMinutes()
                                        );

                                /*
                                 * start <= checkTime < end
                                 */
                                return !start.isAfter(checkTime)
                                        && end.isAfter(checkTime);
                            })
                            .count();

            maxConcurrentUsage =
                    Math.max(
                            maxConcurrentUsage,
                            concurrentUsage
                    );
        }

        return maxConcurrentUsage;
    }
}