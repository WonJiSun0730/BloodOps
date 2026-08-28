package com.shop.changhyeon.reward.service;

import com.shop.reservation.constant.DonationType;

public final class PointRewardPolicy {

    private PointRewardPolicy() {
    }

    public static int pointsFor(DonationType donationType) {
        return switch (donationType) {
            case WHOLE_BLOOD -> 5_000;
            case PLASMA -> 8_000;
            case PLATELET -> 10_000;
            case PLATELET_PLASMA -> 13_000;
        };
    }

    public static String displayName(DonationType donationType) {
        return switch (donationType) {
            case WHOLE_BLOOD -> "전혈";
            case PLASMA -> "혈장";
            case PLATELET -> "혈소판단종";
            case PLATELET_PLASMA -> "혈소판다종";
        };
    }
}
