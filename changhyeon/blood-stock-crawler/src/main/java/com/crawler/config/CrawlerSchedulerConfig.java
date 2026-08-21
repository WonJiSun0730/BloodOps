package com.crawler.config;

import com.crawler.client.BloodStockApiClient;
import com.crawler.model.RawStockData;
import com.crawler.service.BloodInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CrawlerSchedulerConfig {

    private final BloodInfoService bloodInfoService;
    private final BloodStockApiClient apiClient;

    @Scheduled(cron = "${crawler.schedule.cron}")
    public void executeStockSync() {
        log.info("========== 혈액 재고 동기화 배치 작업 시작 ==========");
        List<RawStockData> stockData = bloodInfoService.fetchStockData();
        apiClient.sendStockDataToBackend(stockData);
        log.info("========== 혈액 재고 동기화 배치 작업 종료 ==========");
    }
}