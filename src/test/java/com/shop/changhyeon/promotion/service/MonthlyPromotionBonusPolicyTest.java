package com.shop.changhyeon.promotion.service;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class MonthlyPromotionBonusPolicyTest {
    @Test void day13GetsBonus() { assertEquals(5000, MonthlyPromotionBonusPolicy.bonusFor(LocalDate.of(2026, 9, 13))); }
    @Test void otherDayGetsNoBonus() { assertEquals(0, MonthlyPromotionBonusPolicy.bonusFor(LocalDate.of(2026, 9, 12))); }
}
