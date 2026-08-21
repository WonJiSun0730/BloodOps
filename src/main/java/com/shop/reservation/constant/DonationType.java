package com.shop.reservation.constant;

/**
 * 헌혈 예약 시 선택하는 헌혈 방식입니다.
 * 혈액 재고의 productType(혈액 제제 종류)과는 별개의 개념입니다.
 */
public enum DonationType {

    WHOLE_BLOOD,        //전혈
    PLATELET,           //혈소판
    PLASMA,             //혈장
    PLATELET_PLASMA     //혈소판혈장
}