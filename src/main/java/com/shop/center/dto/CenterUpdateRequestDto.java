package com.shop.center.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CenterUpdateRequestDto {
//공공데이터 기본 필드 5개는 수정 대상에서 제외
    private boolean active;

    private String weekdayHours;
    private String saturdayHours;
    private String sundayHours;
    private String holidayHours;

    private String donationTypes;
    private String amenities;

    private String directions;
    private String parkingInfo;
}