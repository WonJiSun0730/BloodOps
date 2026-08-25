package com.shop.center.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CenterCreateRequestDto {

    private String bloodCenterName;

    @Size(
            max = 100,
            message = "센터명은 100자 이하로 입력해주세요."
    )
    private String centerName;

    private String centerType;

    @Size(
            max = 100,
            message = "주소는 100자 이하로 입력해주세요."
    )
    private String address;

    @Size(
            max = 100,
            message = "연락처는 100자 이하로 입력해주세요."
    )
    private String phone;


    private boolean active = true;


    private String weekdayHours;

    private String saturdayHours;

    private String sundayHours;

    private String holidayHours;

    private String donationTypes;

    private String amenities;

    private String directions;

    private String parkingInfo;
}