package com.shop.changhyeon.promotion.constant;

import java.util.Arrays;
import java.util.Optional;

public enum PromotionRegion {
    SEOUL("서울"),
    GYEONGGI("경기"),
    INCHEON("인천"),
    GANGWON("강원"),
    CHUNGBUK("충북"),
    DAEJEON_SEJONG_CHUNGNAM("대전·세종·충남"),
    DAEGU_GYEONGBUK("대구·경북"),
    GYEONGNAM("경남"),
    ULSAN("울산"),
    BUSAN("부산"),
    JEONBUK("전북"),
    JEONNAM_GWANGJU("전남·광주"),
    JEJU("제주");

    private final String displayName;

    PromotionRegion(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static PromotionRegion from(String value) {
        return find(value)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 지역 구분입니다."));
    }

    public static Optional<PromotionRegion> find(String value) {
        return Arrays.stream(values())
                .filter(region ->
                        region.name().equals(value)
                                || region.displayName.equals(value)
                )
                .findFirst();
    }
}
