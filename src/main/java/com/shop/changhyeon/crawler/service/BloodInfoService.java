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

    private static final String TARGET_URL = "https://www.bloodinfo.net/knredcross/blood_stock.do";

    private final DummyStockGenerator dummyStockGenerator;

    @Value("${crawler.use-dummy:true}")
    private boolean useDummy;

    @Value("${crawler.default-center-id:1}")
    private Long defaultCenterId;

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

            List<RawStockData> result = parseStockTable(doc);
            if (result.isEmpty()) {
                throw new IllegalStateException("혈액 재고 데이터를 추출하지 못했습니다. HTML 구조를 확인하세요.");
            }

            log.info("[크롤러] 혈액 재고 {}건 수집 완료", result.size());
            return result;
        } catch (Exception e) {
            log.error("[크롤러] 데이터 수집 실패", e);
            throw new IllegalStateException("혈액 재고 크롤링에 실패했습니다.", e);
        }
    }

    private List<RawStockData> parseStockTable(Document doc) {
        Elements tables = doc.select("table.stock_table, table");
        for (Element table : tables) {
            List<RawStockData> parsed = parseTable(table);
            if (!parsed.isEmpty()) {
                return parsed;
            }
        }
        return List.of();
    }

    private List<RawStockData> parseTable(Element table) {
        Elements rows = table.select("tbody tr");
        if (rows.isEmpty()) rows = table.select("tr");

        Map<String, Integer> headerIndex = buildHeaderIndex(table);
        List<RawStockData> result = new ArrayList<>();

        for (Element row : rows) {
            Elements cells = row.select("th, td");
            if (cells.isEmpty()) continue;

            Long centerId = extractCenterId(row);
            if (centerId == null) centerId = defaultCenterId;

            // 8종(A+/A-/B+/B-/O+/O-/AB+/AB-) 헤더 기반 표
            if (!headerIndex.isEmpty()) {
                for (Map.Entry<String, Integer> entry : headerIndex.entrySet()) {
                    int index = entry.getValue();
                    if (index >= cells.size()) continue;
                    Integer quantity = parseInteger(cells.get(index).text());
                    if (quantity != null) {
                        result.add(new RawStockData(centerId, entry.getKey(), quantity));
                    }
                }
                continue;
            }

            // 행 자체가 "A+ 10" 같은 구조인 경우
            for (Element cell : cells) {
                BloodTypeToken bloodType = parseBloodType(cell.text());
                if (bloodType == null) continue;

                Integer quantity = parseQuantity(cell.text(), bloodType.value());
                if (quantity != null) {
                    result.add(new RawStockData(centerId, bloodType.value(), quantity));
                }
            }
        }

        return deduplicate(result);
    }

    private Map<String, Integer> buildHeaderIndex(Element table) {
        Map<String, Integer> result = new LinkedHashMap<>();
        Elements headerRows = table.select("thead tr");
        if (headerRows.isEmpty()) headerRows = table.select("tr");
        if (headerRows.isEmpty()) return result;

        // 여러 헤더 행 중 혈액형이 가장 많이 포함된 행을 선택
        Element best = headerRows.stream()
                .max((a, b) -> Integer.compare(countBloodTypes(a), countBloodTypes(b)))
                .orElse(null);
        if (best == null) return result;

        Elements headers = best.select("th, td");
        for (int i = 0; i < headers.size(); i++) {
            BloodTypeToken token = parseBloodType(headers.get(i).text());
            if (token != null) result.putIfAbsent(token.value(), i);
        }
        return result;
    }

    private int countBloodTypes(Element row) {
        int count = 0;
        for (Element cell : row.select("th, td")) {
            if (parseBloodType(cell.text()) != null) count++;
        }
        return count;
    }

    private Integer parseQuantity(String text, String bloodType) {
        String normalized = text.replace(bloodType, "");
        return parseInteger(normalized);
    }

    private Long extractCenterId(Element element) {
        String[] keys = {"data-center-id", "data-centerid", "center-id", "centerId"};
        for (String key : keys) {
            String value = element.attr(key);
            if (!value.isBlank()) {
                try {
                    return Long.valueOf(value.trim());
                } catch (NumberFormatException ignored) {
                    log.debug("[크롤러] centerId 파싱 실패: {}={}", key, value);
                }
            }
        }
        return null;
    }

    private Integer parseInteger(String text) {
        String digits = text.replaceAll("[^0-9-]", "");
        if (digits.isBlank() || "-".equals(digits)) return null;
        try {
            return Integer.valueOf(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalize(String text) {
        return text == null ? "" : text.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private BloodTypeToken parseBloodType(String text) {
        String value = normalize(text);
        return switch (value) {
            case "A+", "A＋", "A_POSITIVE", "A형+", "A형양성" -> new BloodTypeToken("A_POSITIVE");
            case "A-", "A－", "A_NEGATIVE", "A형-", "A형음성" -> new BloodTypeToken("A_NEGATIVE");
            case "B+", "B＋", "B_POSITIVE", "B형+", "B형양성" -> new BloodTypeToken("B_POSITIVE");
            case "B-", "B－", "B_NEGATIVE", "B형-", "B형음성" -> new BloodTypeToken("B_NEGATIVE");
            case "O+", "O＋", "O_POSITIVE", "O형+", "O형양성" -> new BloodTypeToken("O_POSITIVE");
            case "O-", "O－", "O_NEGATIVE", "O형-", "O형음성" -> new BloodTypeToken("O_NEGATIVE");
            case "AB+", "AB＋", "AB_POSITIVE", "AB형+", "AB형양성" -> new BloodTypeToken("AB_POSITIVE");
            case "AB-", "AB－", "AB_NEGATIVE", "AB형-", "AB형음성" -> new BloodTypeToken("AB_NEGATIVE");
            default -> null;
        };
    }

    private List<RawStockData> deduplicate(List<RawStockData> data) {
        Map<String, RawStockData> unique = new LinkedHashMap<>();
        for (RawStockData item : data) {
            if (item.getCenterId() == null || item.getBloodType() == null || item.getQuantity() == null) continue;
            unique.put(item.getCenterId() + ":" + item.getBloodType(), item);
        }
        return new ArrayList<>(unique.values());
    }

    private record BloodTypeToken(String value) {}
}
