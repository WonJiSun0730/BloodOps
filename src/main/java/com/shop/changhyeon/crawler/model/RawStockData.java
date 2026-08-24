package com.shop.changhyeon.crawler.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RawStockData {
    private Long centerId;
    private String bloodType;
    private Integer quantity;
    private String productType;

    @Builder
    public RawStockData(Long centerId, String bloodType, Integer quantity, String productType) {
        this.centerId = centerId;
        this.bloodType = bloodType;
        this.quantity = quantity;
        this.productType = productType;
    }

    public RawStockData(Long centerId, String bloodType, Integer quantity) {
        this(centerId, bloodType, quantity, "전혈");
    }
}
