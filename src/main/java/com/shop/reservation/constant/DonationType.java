package com.shop.reservation.constant;

/**
 * 헌혈 예약 시 선택하는 헌혈 방식입니다.
 *
 * 혈액 재고의 productType과는 별개이며,
 * 예약 처리에서는 어떤 장비그룹을 사용하는지와
 * 실제 장비 점유시간도 함께 관리합니다.
 */
public enum DonationType {

    WHOLE_BLOOD(
            EquipmentGroupType.WHOLE_BLOOD_PLASMA,
            30
    ),

    PLATELET(
            EquipmentGroupType.PLATELET,
            90
    ),

    PLASMA(
            EquipmentGroupType.WHOLE_BLOOD_PLASMA,
            60
    ),

    PLATELET_PLASMA(
            EquipmentGroupType.PLATELET,
            90
    );


    private final EquipmentGroupType
            equipmentGroupType;

    private final int
            equipmentUsageMinutes;


    DonationType(
            EquipmentGroupType equipmentGroupType,
            int equipmentUsageMinutes
    ) {
        this.equipmentGroupType =
                equipmentGroupType;

        this.equipmentUsageMinutes =
                equipmentUsageMinutes;
    }


    public EquipmentGroupType
    getEquipmentGroupType() {
        return equipmentGroupType;
    }


    public int
    getEquipmentUsageMinutes() {
        return equipmentUsageMinutes;
    }
}