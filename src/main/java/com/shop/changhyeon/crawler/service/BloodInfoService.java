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

    /** 대한적십자사 혈액관리본부 혈액보유현황 페이지 */
    private static final String TARGET_URL = "https://bloodinfo.net/knrcbs/main.do";

    /** 공식 전국 현황을 blood_stocks에 저장할 때 사용하는 예약 centerId */
    public static final long NATIONAL_CENTER_ID = 0L;

    private final DummyStockGenerator dummyStockGenerator;

    @Value("${crawler.use-dummy:false}")
    private boolean useDummy;

    @Value("${crawler.timeout-ms:10000}")
    private int timeoutMs;

    public List<RawStockData> fetchStockData() {
        if (useDummy) {
            log.info("[크롤러] 더미 모드로 데이터를 생성합니다.");
            return dummyStockGenerator.generateDummyData();
        }

        try {
            Document doc = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0 BloodOpsCrawler/1.0")
                    .timeout(timeoutMs)
                    .get();

            List<RawStockData> result = parseOfficialStockTable(doc);
            if (result.isEmpty()) {
                throw new IllegalStateException("대한적십자사 공식 혈액보유현황에서 현재 혈액보유량을 추출하지 못했습니다. HTML 구조를 확인하세요.");
            }

            log.info("[크롤러] 대한적십자사 공식 혈액보유량 {}건 수집 완료", result.size());
            return result;
        } catch (Exception e) {
            log.error("[크롤러] 대한적십자사 혈액보유현황 수집 실패", e);
            throw new IllegalStateException("대한적십자사 혈액보유현황 크롤링에 실패했습니다.", e);
        }
    }

    /**
     * 공식 페이지의 '적혈구제제 보유 현황' 표에서 '현재 혈액보유량' 행만 읽는다.
     * 표 구조는 합계/A형/B형/O형/AB형이며, 합계는 DB에 저장하지 않는다.
     */
    private List<RawStockData> parseOfficialStockTable(Document doc) {
        Elements tables = doc.select("table");
        for (Element table : tables) {
            List<RawStockData> parsed = parseOfficialTable(table);
            if (!parsed.isEmpty()) {
                return parsed;
            }
        }
        return List.of();
    }

    private List<RawStockData> parseOfficialTable(Element table) {
        Elements rows = table.select("tr");
        if (rows.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> bloodTypeIndexes = findOfficialHeaderIndexes(rows);
        if (bloodTypeIndexes.size() != 4) {
            return List.of();
        }

        Element currentStockRow = findCurrentStockRow(rows);
        if (currentStockRow == null) {
            return List.of();
        }

        Elements cells = currentStockRow.select("th, td");
        List<RawStockData> result = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : bloodTypeIndexes.entrySet()) {
            int index = entry.getValue();
            if (index >= cells.size()) {
                return List.of();
            }

            Integer quantity = parseInteger(cells.get(index).text());
            if (quantity == null || quantity < 0) {
                return List.of();
            }

            result.add(new RawStockData(NATIONAL_CENTER_ID, entry.getKey(), quantity));
        }

        return deduplicate(result);
    }

    private Map<String, Integer> findOfficialHeaderIndexes(Elements rows) {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (Element row : rows) {
            Elements cells = row.select("th, td");
            Map<String, Integer> candidate = new LinkedHashMap<>();

            for (int i = 0; i < cells.size(); i++) {
                String bloodType = parseOfficialBloodType(cells.get(i).text());
                if (bloodType != null) {
                    candidate.putIfAbsent(bloodType, i);
                }
            }

            if (candidate.size() == 4) {
                result.putAll(candidate);
                return result;
            }
        }

        return result;
    }

    private Element findCurrentStockRow(Elements rows) {
        for (Element row : rows) {
            String text = normalize(row.text());
            if (text.contains("현재혈액보유량") || text.contains("현재보유량")) {
                return row;
            }
        }
        return null;
    }

    private String parseOfficialBloodType(String text) {
        String value = normalize(text);
        return switch (value) {
            case "A", "A형" -> "A";
            case "B", "B형" -> "B";
            case "O", "O형" -> "O";
            case "AB", "AB형" -> "AB";
            default -> null;
        };
    }

    private Integer parseInteger(String text) {
        String digits = text.replaceAll("[^0-9-]", "");
        if (digits.isBlank() || "-".equals(digits)) {
            return null;
        }
        try {
            return Integer.valueOf(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalize(String text) {
        return text == null ? "" : text.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private List<RawStockData> deduplicate(List<RawStockData> data) {
        Map<String, RawStockData> unique = new LinkedHashMap<>();
        for (RawStockData item : data) {
            if (item.getCenterId() == null || item.getBloodType() == null || item.getQuantity() == null) {
                continue;
            }
            unique.put(item.getCenterId() + ":" + item.getBloodType(), item);
        }
        return new ArrayList<>(unique.values());
    }
}
