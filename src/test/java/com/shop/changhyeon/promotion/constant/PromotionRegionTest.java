package com.shop.changhyeon.promotion.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromotionRegionTest {

    @Test
    void supportsAllConfiguredRegions() {
        assertEquals(13, PromotionRegion.values().length);
        assertEquals(
                "대전·세종·충남",
                PromotionRegion.DAEJEON_SEJONG_CHUNGNAM.displayName()
        );
        assertEquals(
                PromotionRegion.JEONNAM_GWANGJU,
                PromotionRegion.from("전남·광주")
        );
    }

    @Test
    void rejectsUnsupportedRegion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PromotionRegion.from("기타")
        );
    }
}
