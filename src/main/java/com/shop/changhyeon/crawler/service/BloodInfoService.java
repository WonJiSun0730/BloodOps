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

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloodInfoService {

    private static final String TARGET_URL = "https://www.bloodinfo.net/knrcbs/bi/info/bldHoldSttus.do?mi=1046";
    public static final Long NATIONAL_CENTER_ID = 0L;
    private static final List<String> BLOOD_TYPES = List.of("A", "B", "O", "AB");

    private final DummyStockGenerator dummyStockGenerator;

    @Value("${crawler.use-dummy:true}")
    private boolean useDummy;

    @Value("${crawler.timeout-ms:10000}")
    private int timeoutMs;

    public List<RawStockData> fetchStockData() {
        if (useDummy) {
            log.info("[크롤러] 더미 모드로 혈액보유현황을 생성합니다.");
            return dummyStockGenerator.generateDummyData();
        }

        log.info("[크롤러] 대한적십자사 혈액보유현황 크롤링 시작 target={}", TARGET_URL);

        try {
            Document doc = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/131.0 BloodOpsCrawler/1.0")
                    .referrer("https://www.bloodinfo.net/")
                    .timeout(timeoutMs)
                    .get();

            List<RawStockData> result = parseBloodStock(doc);
            validateResult(result);

            log.info("[크롤러] 혈액보유현황 크롤링 완료: {}건", result.size());
            result.forEach(stock -> log.info("[크롤러] centerId={}, bloodType={}, quantity={}",
                    stock.getCenterId(), stock.getBloodType(), stock.getQuantity()));

            return result;
        } catch (Exception e) {
            log.error("[크롤러] 대한적십자사 혈액보유현황 크롤링 실패", e);
            throw new IllegalStateException("대한적십자사 혈액보유현황 크롤링에 실패했습니다.", e);
        }
    }

    private List<RawStockData> parseBloodStock(Document document) {
        Elements tables = document.select("table");
        if (tables.isEmpty()) return List.of();

        // 1차: 혈액보유현황 전용 표 식별 시도 후 파싱
        for (Element table : tables) {
            if (isBloodStockTable(table)) {
                List<RawStockData> result = parseStockTable(table);
                if (!result.isEmpty()) return result;
            }
        }
        // 2차: 식별 실패 시 전체 표 순회 파싱
        for (Element table : tables) {
            List<RawStockData> result = parseStockTable(table);
            if (!result.isEmpty()) return result;
        }

        return List.of();
    }

    private boolean isBloodStockTable(Element table) {
        String text = normalize(table.text());
        boolean hasBloodType = BLOOD_TYPES.stream().anyMatch(type -> text.contains(type + "형") || text.contains(type));
        boolean hasCurrentStock = text.contains("현재혈액보유량") || text.contains("현재보유량") || text.contains("혈액보유량");
        return hasBloodType && hasCurrentStock;
    }

    private List<RawStockData> parseStockTable(Element table) {
        Elements rows = table.select("tr");
        if (rows.isEmpty()) return List.of();

        Map<String, Integer> colMap = findBloodTypeColumns(rows);
        if (colMap.size() < 4) return List.of();

        Element targetRow = findCurrentStockRow(rows);
        if (targetRow == null) return List.of();

        Elements cells = targetRow.select("th, td");
        List<RawStockData> result = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : colMap.entrySet()) {
            int idx = entry.getValue();
            if (idx >= cells.size()) continue;

            Integer quantity = parseQuantity(cells.get(idx).text());
            if (quantity != null) {
                result.add(new RawStockData(NATIONAL_CENTER_ID, entry.getKey(), quantity));
            }
        }

        return result.size() == 4 ? result : List.of();
    }

    private Map<String, Integer> findBloodTypeColumns(Elements rows) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Element row : rows) {
            Elements cells = row.select("th, td");
            for (int i = 0; i < cells.size(); i++) {
                String bloodType = parseBloodType(cells.get(i).text());
                if (bloodType != null) {
                    result.putIfAbsent(bloodType, i);
                }
            }
            if (result.size() == 4) break;
        }
        return result;
    }

    private Element findCurrentStockRow(Elements rows) {
        return rows.stream()
                .filter(row -> {
                    String text = normalize(row.text());
                    return text.contains("현재혈액보유량") || text.contains("현재보유량");
                })
                .findFirst()
                .orElse(null);
    }

    private String parseBloodType(String text) {
        String val = normalize(text);
        return switch (val) {
            case "A", "A형" -> "A";
            case "B", "B형" -> "B";
            case "O", "O형" -> "O";
            case "AB", "AB형" -> "AB";
            default -> null;
        };
    }

    private Integer parseQuantity(String text) {
        if (text == null || text.isBlank()) return null;
        String value = text.replace(",", "").replaceAll("[^0-9-]", "");
        if (value.isBlank() || "-".equals(value)) return null;

        try {
            int quantity = Integer.parseInt(value);
            return quantity >= 0 ? quantity : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalize(String text) {
        return text == null ? "" : text.replace('\u00A0', ' ').replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private void validateResult(List<RawStockData> result) {
        if (result.size() != 4) {
            throw new IllegalStateException("혈액보유현황 데이터가 4건이 아닙니다. count=" + result.size());
        }

        long distinctTypes = result.stream()
                .peek(stock -> {
                    if (!NATIONAL_CENTER_ID.equals(stock.getCenterId()) || stock.getQuantity() == null || stock.getQuantity() < 0) {
                        throw new IllegalStateException("유효하지 않은 혈액보유 데이터 존재");
                    }
                })
                .map(RawStockData::getBloodType)
                .distinct()
                .count();

        if (distinctTypes != 4) {
            throw new IllegalStateException("혈액형 중복 또는 누락이 존재합니다.");
        }
    }
}