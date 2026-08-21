package com.shop.changhyeon.crawler.client;

import com.shop.changhyeon.crawler.model.RawStockData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BloodStockApiClient {

    private final RestTemplate restTemplate;

    @Value("${backend.api.url}")
    private String backendUrl;

    public void sendStockDataToBackend(List<RawStockData> stockDataList) {
        if (stockDataList.isEmpty()) {
            log.warn("[API Client] 전송할 재고 데이터가 없습니다.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<RawStockData>> request = new HttpEntity<>(stockDataList, headers);
            restTemplate.postForEntity(backendUrl, request, String.class);

            log.info("[API Client] 성공적으로 백엔드 서버({})에 {}건의 재고 데이터를 전송했습니다.", backendUrl, stockDataList.size());
        } catch (Exception e) {
            log.error("[API Client] 백엔드 데이터 전송 실패: {}", e.getMessage());
        }
    }
}