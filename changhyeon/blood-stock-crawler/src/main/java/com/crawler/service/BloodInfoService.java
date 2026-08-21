package com.crawler.service;

import com.crawler.model.RawStockData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloodInfoService {

    private final DummyStockGenerator dummyStockGenerator;

    @Value("${crawler.use-dummy:true}")
    private boolean useDummy;

    public List<RawStockData> fetchStockData() {
        if (useDummy) {
            log.info("[크롤러] 더미 모드로 데이터를 생성합니다.");
            return dummyStockGenerator.generateDummyData();
        }

        log.info("[크롤러] 웹 수집을 시작합니다.");
        List<RawStockData> crawledData = new ArrayList<>();

        try {
            String targetUrl = "https://www.bloodinfo.net/knredcross/blood_stock.do";
            Document doc = Jsoup.connect(targetUrl).get();

            Elements rows = doc.select("table.stock_table tbody tr");
            for (Element row : rows) {
                // 파싱 로직 구현 영역
            }
        } catch (Exception e) {
            log.error("[크롤러] 데이터 수집 중 오류 발생: {}", e.getMessage());
        }

        return crawledData;
    }
}