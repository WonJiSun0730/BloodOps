package com.shop.changhyeon.crawler.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record BloodStockStatusResponse(
        LocalDateTime updatedAt,
        ProductStatus redBloodCell,
        ProductStatus apheresisPlatelet,
        CrisisStatus crisis
) {
    public record ProductStatus(
            String name,
            String metricUnit,
            StockMetric total,
            Map<String, StockMetric> bloodTypes
    ) {
    }

    public record StockMetric(
            int dailyUsage,
            int currentStock,
            double value
    ) {
    }

    public record CrisisStatus(
            String level,
            String criterion,
            String message
    ) {
    }
}
