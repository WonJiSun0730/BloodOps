package com.shop.center.dto;

import com.shop.center.entity.Center;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CenterMapResponseDto {

    private Long id;
    private String centerName;

    private Double latitude;
    private Double longitude;

    private boolean active;


    public static CenterMapResponseDto from(
            Center center
    ) {

        return CenterMapResponseDto.builder()
                .id(center.getId())
                .centerName(center.getCenterName())
                .latitude(center.getLatitude())
                .longitude(center.getLongitude())
                .active(center.isActive())
                .build();
    }
}