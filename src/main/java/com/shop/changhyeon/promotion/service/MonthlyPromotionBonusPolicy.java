package com.shop.changhyeon.promotion.service;
import java.time.LocalDate;
public final class MonthlyPromotionBonusPolicy {
    public static final int BONUS_POINTS = 5_000;
    private MonthlyPromotionBonusPolicy() {}
    public static int bonusFor(LocalDate date) { return date != null && date.getDayOfMonth() == 13 ? BONUS_POINTS : 0; }
}
