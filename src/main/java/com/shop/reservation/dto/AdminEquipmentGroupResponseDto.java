package com.shop.reservation.dto;

import com.shop.reservation.constant.EquipmentGroupType;
import com.shop.reservation.entity.EquipmentGroup;
import lombok.Getter;

@Getter
public class AdminEquipmentGroupResponseDto {

    private final Long id;

    private final Long centerId;

    private final String centerName;

    private final EquipmentGroupType groupType;

    private final int capacity;

    private final boolean active;


    public AdminEquipmentGroupResponseDto(
            EquipmentGroup equipmentGroup
    ) {
        this.id =
                equipmentGroup.getId();

        this.centerId =
                equipmentGroup
                        .getCenter()
                        .getId();

        this.centerName =
                equipmentGroup
                        .getCenter()
                        .getCenterName();

        this.groupType =
                equipmentGroup
                        .getGroupType();

        this.capacity =
                equipmentGroup
                        .getCapacity();

        this.active =
                equipmentGroup
                        .isActive();
    }
}