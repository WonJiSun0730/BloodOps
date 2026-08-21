package com.shop.changhyeon.management.dto;

public record StockSummaryDto(
        String productType,
        String bloodType,
        int count
) {}