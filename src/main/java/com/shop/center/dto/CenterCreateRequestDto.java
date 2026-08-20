package com.shop.center.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CenterCreateRequestDto {

    private String bloodCenterName;

    private String centerName;

    private String centerType;

    private String address;

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