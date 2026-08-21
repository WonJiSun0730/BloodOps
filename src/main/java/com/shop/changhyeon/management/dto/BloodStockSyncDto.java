package com.shop.changhyeon.management.dto;

import com.shop.changhyeon.management.constant.BloodType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BloodStockSyncDto {
    private Long centerId;
    private BloodType bloodType;
    private Integer quantity;
}