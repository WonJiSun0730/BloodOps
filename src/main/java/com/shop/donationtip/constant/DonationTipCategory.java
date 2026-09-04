package com.shop.donationtip.constant;

public enum DonationTipCategory {

    BEFORE("헌혈 전 주의사항"),
    AFTER("헌혈 후 주의사항");

    private final String label;

    DonationTipCategory(
            String label
    ) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}