package com.shop.changhyeon.management.dto;

import com.shop.center.entity.Center;

public record BloodUnitCenterDto(
        Long id,
        String centerName,
        String address
) {
    public static BloodUnitCenterDto from(Center center) {
        return new BloodUnitCenterDto(
                center.getId(),
                center.getCenterName(),
                center.getAddress()
        );
    }
}
