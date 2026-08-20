package com.shop.center.dto;

import com.shop.center.constant.CenterSource;
import com.shop.center.entity.Center;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CenterResponseDto {
//    Entity를 React에 그대로 내보내지 않기 위한 응답용 DTO
    //나중에 상세조회에서도 재사용 가능
    private Long id;

    private String bloodCenterName;
    private String centerName;
    private String centerType;
    private String address;
    private String phone;

    private CenterSource source;

    private boolean active;

    private String weekdayHours;
    private String saturdayHours;
    private String sundayHours;
    private String holidayHours;

    private String donationTypes;
    private String amenities;

    private String directions;
    private String parkingInfo;


    public static CenterResponseDto from(Center center) {

        return CenterResponseDto.builder()
                .id(center.getId())
                .bloodCenterName(center.getBloodCenterName())
                .centerName(center.getCenterName())
                .centerType(center.getCenterType())
                .address(center.getAddress())
                .phone(center.getPhone())
                .source(center.getSource())
                .active(center.isActive())
                .weekdayHours(center.getWeekdayHours())
                .saturdayHours(center.getSaturdayHours())
                .sundayHours(center.getSundayHours())
                .holidayHours(center.getHolidayHours())
                .donationTypes(center.getDonationTypes())
                .amenities(center.getAmenities())
                .directions(center.getDirections())
                .parkingInfo(center.getParkingInfo())
                .build();
    }
}