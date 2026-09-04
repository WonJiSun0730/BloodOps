package com.shop.halloffame.dto;

public interface DonationCountProjection {

    Long getMemberId();

    String getMemberName();

    Long getDonationCount();
}