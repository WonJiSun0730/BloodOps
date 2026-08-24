package com.shop.changhyeon.crawler.service;

import com.shop.changhyeon.crawler.model.RawStockData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloodInfoService {

    private static final String TARGET_URL = "https://bloodinfo.net/knrcbs/main.do";
    public static final long NATIONAL_CENTER_ID = 0L;

    private final DummyStockGenerator dummyStockGenerator;

    @Value("${crawler.use-dummy:false}")
    private boolean useDummy;

    @Value("${crawler.timeout-ms:10000}")
    private int timeoutMs;

    public List<RawStockData> fetchStockData() {
        if (useDummy) {
            return dummyStockGenerator.generateDummyData();
        }

        try {
            Document doc = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0 BloodOpsCrawler/1.0")
                    .timeout(timeoutMs)
                    .get();

            List<RawStockData> result = parseOfficialStockTable(doc);
            validateOfficialResult(result);
            log.info("[크롤러] 대한적십자사 공식 혈액보유량 수집 완료: {}", result);
            return result;
        } catch (Exception e) {
            log.error("[크롤러] 대한적십자사 혈액보유현황 수집 실패", e);
            throw new IllegalStateException("대한적십자사 혈액보유현황 크롤링에 실패했습니다.", e);
        }
    }

    /**
     * 공식 적혈구제제 표에서 '현재 혈액보유량'의 Unit 값을 읽는다.
     * 공식 공개 혈액형은 A/B/O/AB 4종이다.
     */
    private List<RawStockData> parseOfficialStockTable(Document doc) {
        for (Element table : doc.select("table")) {
            String tableText = normalize(table.text());
            if (!tableText.contains("적혈구제제")) {
                continue;
            }
            List<RawStockData> parsed = parseOfficialTable(table);
            if (!parsed.isEmpty()) {
                return parsed;
            }
        }
        return List.of();
    }

    private List<RawStockData> parseOfficialTable(Element table) {
        Elements rows = table.select("tr");
        Element currentRow = findCurrentStockRow(rows);
        if (currentRow == null) {
            return List.of();
        }

        /*
         * 공식 표의 현재 혈액보유량 행은
         *   현재 혈액보유량 | 합계 | A형 | B형 | O형 | AB형
         * 순서다. rowspan/colspan 때문에 header의 DOM index를 그대로
         * 데이터 행에 적용하지 않고, 현재 행의 숫자 5개를 직접 읽는다.
         */
        List<Integer> values = numericCells(currentRow);
        if (values.size() != 5) {
            log.warn("[크롤러] 현재 혈액보유량 행의 숫자 개수가 5개가 아닙니다. values={}, row={}", values, currentRow.text());
            return List.of();
        }

        int total = values.get(0);
        int a = values.get(1);
        int b = values.get(2);
        int o = values.get(3);
        int ab = values.get(4);

        // 공식 표의 합계와 혈액형 4종 합계가 다르면 잘못된 표를 읽은 것으로 보고 저장하지 않는다.
        if (total != a + b + o + ab) {
            log.warn("[크롤러] 공식 혈액보유량 합계 불일치. total={}, detailSum={}", total, a + b + o + ab);
            return List.of();
        }

        return List.of(
                new RawStockData(NATIONAL_CENTER_ID, "A", a),
                new RawStockData(NATIONAL_CENTER_ID, "B", b),
                new RawStockData(NATIONAL_CENTER_ID, "O", o),
                new RawStockData(NATIONAL_CENTER_ID, "AB", ab)
        );
    }

    private Element findCurrentStockRow(Elements rows) {
        for (Element row : rows) {
            String text = normalize(row.text());
            if (text.contains("현재혈액보유량")) {
                return row;
            }
        }
        return null;
    }

    private List<Integer> numericCells(Element row) {
        List<Integer> values = new ArrayList<>();
        for (Element cell : row.select("th, td")) {
            String text = cell.text().replace(",", "").trim();
            if (!text.matches("\\d+")) {
                continue;
            }
            try {
                values.add(Integer.parseInt(text));
            } catch (NumberFormatException e) {
                return List.of();
            }
        }
        return values;
    }

    private void validateOfficialResult(List<RawStockData> result) {
        if (result.size() != 4) {
            throw new IllegalStateException("공식 혈액보유현황은 A/B/O/AB 4건이어야 합니다. actual=" + result.size());
        }

        Map<String, RawStockData> byType = new LinkedHashMap<>();
        for (RawStockData item : result) {
            if (item.getCenterId() == null || item.getCenterId() != NATIONAL_CENTER_ID
                    || item.getBloodType() == null || item.getQuantity() == null || item.getQuantity() < 0) {
                throw new IllegalStateException("공식 혈액보유현황 데이터가 유효하지 않습니다: " + item);
            }
            byType.put(item.getBloodType(), item);
        }

        if (!byType.keySet().containsAll(List.of("A", "B", "O", "AB"))) {
            throw new IllegalStateException("공식 혈액보유현황 혈액형이 A/B/O/AB로 완성되지 않았습니다: " + byType.keySet());
        }
    }

    private String normalize(String text) {
        return text == null ? "" : text.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}
