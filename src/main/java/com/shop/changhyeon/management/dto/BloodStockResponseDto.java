package com.shop.changhyeon.management.dto;

import com.shop.changhyeon.management.constant.BloodType;
import com.shop.changhyeon.management.entity.BloodStock;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodStockResponseDto {
    private Long stockId;
    private Long centerId;
    private BloodType bloodType;
    private Integer quantity;

    public static BloodStockResponseDto from(BloodStock entity) {
        return BloodStockResponseDto.builder()
                .stockId(entity.getId())
                .centerId(entity.getCenterId())
                .bloodType(entity.getBloodType())
                .quantity(entity.getQuantity())
                .build();
    }
}