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
        return fetchOfficialStockData();
    }

    public List<RawStockData> fetchOfficialStockData() {
        return fetchDocumentAndParse().stream()
                .filter(stock -> "전혈".equals(stock.getProductType()))
                .toList();
    }

    /**
     * 공식 페이지의 적혈구/농축혈소판/성분채혈혈소판 보유량을 모두 반환한다.
     * 화면 명칭과 프로젝트 명칭을 다음과 같이 매핑한다.
     * 적혈구제제 -> 전혈, 농축혈소판 -> 혈소판다종, 성분채혈혈소판 -> 혈소판단종
     */
    public List<RawStockData> fetchOfficialProductStockData() {
        return fetchDocumentAndParse();
    }

    private List<RawStockData> fetchDocumentAndParse() {
        log.info("[크롤러] 대한적십자사 혈액보유현황 크롤링 시작 target={}", TARGET_URL);

        try {
            Document doc = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/131.0 BloodOpsCrawler/1.0")
                    .referrer("https://www.bloodinfo.net/")
                    .timeout(timeoutMs)
                    .get();

            List<RawStockData> result = parseProductStock(doc);
            validateProductResult(result);

            log.info("[크롤러] 제제별 혈액보유현황 크롤링 완료: {}건", result.size());
            return result;
        } catch (Exception e) {
            log.error("[크롤러] 대한적십자사 혈액보유현황 크롤링 실패", e);
            throw new IllegalStateException("대한적십자사 혈액보유현황 크롤링에 실패했습니다.", e);
        }
    }

    private List<RawStockData> parseProductStock(Document document) {
        List<RawStockData> result = new ArrayList<>();

        for (Element table : document.select("table")) {
            String productType = detectProductType(table);
            if (productType == null) continue;

            List<RawStockData> stock = parseStockTable(table, productType);
            if (stock.size() == 4) result.addAll(stock);
        }

        return result;
    }

    private String detectProductType(Element table) {
        String text = normalize(table.text());
        if (text.contains("적혈구제제보유현황")) return "전혈";
        if (text.contains("농축혈소판")) return "혈소판다종";
        if (text.contains("성분채혈혈소판")) return "혈소판단종";
        return null;
    }

    private List<RawStockData> parseStockTable(Element table, String productType) {
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
                result.add(new RawStockData(NATIONAL_CENTER_ID, entry.getKey(), quantity, productType));
            }
        }

        return result.size() == 4 ? result : List.of();
    }

    private List<RawStockData> parseBloodStock(Document document) {
        Elements tables = document.select("table");
        if (tables.isEmpty()) return List.of();

        for (Element table : tables) {
            if (isBloodStockTable(table)) {
                List<RawStockData> result = parseStockTable(table, "전혈");
                if (!result.isEmpty()) return result;
            }
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
        return parseStockTable(table, "전혈");
    }

    private Map<String, Integer> findBloodTypeColumns(Elements rows) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Element row : rows) {
            Elements cells = row.select("th, td");
            for (int i = 0; i < cells.size(); i++) {
                String bloodType = parseBloodType(cells.get(i).text());
                if (bloodType != null) result.putIfAbsent(bloodType, i);
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

        if (distinctTypes != 4) throw new IllegalStateException("혈액형 중복 또는 누락이 존재합니다.");
    }

    private void validateProductResult(List<RawStockData> result) {
        if (result.size() != 12) {
            throw new IllegalStateException("제제별 혈액보유현황 데이터가 12건이 아닙니다. count=" + result.size());
        }

        Map<String, Long> productCounts = result.stream()
                .peek(stock -> {
                    if (!NATIONAL_CENTER_ID.equals(stock.getCenterId()) || stock.getQuantity() == null || stock.getQuantity() < 0) {
                        throw new IllegalStateException("유효하지 않은 혈액보유 데이터 존재");
                    }
                })
                .collect(java.util.stream.Collectors.groupingBy(RawStockData::getProductType, java.util.stream.Collectors.counting()));

        for (String product : List.of("전혈", "혈소판단종", "혈소판다종")) {
            if (!Long.valueOf(4L).equals(productCounts.get(product))) {
                throw new IllegalStateException("제제별 혈액형 데이터가 누락되었습니다: " + product);
            }
        }
    }
}
