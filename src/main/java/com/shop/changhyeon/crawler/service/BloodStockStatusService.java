package com.shop.changhyeon.crawler.service;

import com.shop.changhyeon.crawler.dto.BloodStockStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class BloodStockStatusService {

    private static final String TARGET_URL =
            "https://www.bloodinfo.net/knrcbs/bi/info/bldHoldSttus.do?mi=1046";
    private static final List<String> BLOOD_TYPES = List.of("A", "B", "O", "AB");

    @Value("${crawler.timeout-ms:30000}")
    private int timeoutMs;

    public BloodStockStatusResponse fetchStatus() {
        try {
            Document document = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .referrer("https://www.bloodinfo.net/")
                    .timeout(timeoutMs)
                    .get();

            return parseStatus(document);
        } catch (Exception exception) {
            log.error("[혈액 보유상태] 크롤링 실패", exception);
            throw new IllegalStateException("혈액 보유상태를 불러오지 못했습니다.", exception);
        }
    }

    BloodStockStatusResponse parseStatus(Document document) {
        ProductRows redBloodCell = findProductRows(document, "적혈구제제");
        ProductRows concentratedPlatelet = findProductRows(document, "농축혈소판");
        ProductRows apheresisPlatelet = findProductRows(document, "성분채혈혈소판");

        BloodStockStatusResponse.ProductStatus redStatus =
                toProductStatus("적혈구제제", "일", redBloodCell, false);
        BloodStockStatusResponse.ProductStatus concentratedPlateletStatus =
                toProductStatus("농축혈소판", "%", concentratedPlatelet, true);
        BloodStockStatusResponse.ProductStatus plateletStatus =
                toProductStatus("성분채혈혈소판", "%", apheresisPlatelet, true);

        return new BloodStockStatusResponse(
                LocalDateTime.now(),
                redStatus,
                concentratedPlateletStatus,
                plateletStatus,
                crisisStatus(redStatus.total().value())
        );
    }

    private ProductRows findProductRows(Document document, String productName) {
        for (Element table : document.select("table")) {
            String tableText = normalize(table.text());
            String headingText = previousHeadingText(table);

            if (!tableText.contains(normalize(productName))
                    && !headingText.contains(normalize(productName))) {
                continue;
            }

            Map<String, Integer> columns = findColumns(table.select("tr"));
            if (columns.size() != BLOOD_TYPES.size()) {
                continue;
            }

            Element dailyUsage = null;
            Element currentStock = null;
            boolean inProductSection = !tableText.contains("농축혈소판")
                    || "적혈구제제".equals(productName);

            for (Element row : table.select("tr")) {
                String rowText = normalize(row.text());

                if (rowText.contains("농축혈소판")) {
                    inProductSection = "농축혈소판".equals(productName);
                }
                if (rowText.contains("성분채혈혈소판")) {
                    inProductSection = "성분채혈혈소판".equals(productName);
                }
                if (!inProductSection) {
                    continue;
                }
                if (rowText.contains("1일소요량")) {
                    dailyUsage = row;
                } else if (rowText.contains("현재혈액보유량")
                        || rowText.contains("현재보유량")) {
                    currentStock = row;
                }
            }

            if (dailyUsage != null && currentStock != null) {
                return new ProductRows(
                        parseRow(dailyUsage, columns),
                        parseRow(currentStock, columns)
                );
            }
        }

        throw new IllegalStateException(productName + "의 1일 소요량 또는 현재 보유량을 찾지 못했습니다.");
    }

    private Map<String, Integer> findColumns(Elements rows) {
        Map<String, Integer> columns = new LinkedHashMap<>();

        for (Element row : rows) {
            Elements cells = row.select("th, td");
            for (int index = 0; index < cells.size(); index++) {
                String value = normalize(cells.get(index).text());
                for (String bloodType : BLOOD_TYPES) {
                    if (value.equals(bloodType) || value.equals(bloodType + "형")) {
                        columns.putIfAbsent(bloodType, index);
                    }
                }
            }
            if (columns.size() == BLOOD_TYPES.size()) {
                break;
            }
        }
        return columns;
    }

    private Map<String, Integer> parseRow(Element row, Map<String, Integer> columns) {
        Elements cells = row.select("th, td");
        Map<String, Integer> values = new LinkedHashMap<>();

        int lastColumnIndex = columns.values()
                .stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElseThrow();
        int leadingCellOffset = Math.max(
                0,
                cells.size() - (lastColumnIndex + 1)
        );

        for (String bloodType : BLOOD_TYPES) {
            int index = columns.get(bloodType) + leadingCellOffset;
            if (index >= cells.size()) {
                throw new IllegalStateException(bloodType + "형 수치가 누락되었습니다.");
            }
            values.put(bloodType, parseNumber(cells.get(index).text()));
        }
        return values;
    }

    private BloodStockStatusResponse.ProductStatus toProductStatus(
            String name,
            String unit,
            ProductRows rows,
            boolean percentage
    ) {
        Map<String, BloodStockStatusResponse.StockMetric> bloodTypes = new LinkedHashMap<>();
        int totalDailyUsage = 0;
        int totalCurrentStock = 0;

        for (String bloodType : BLOOD_TYPES) {
            int dailyUsage = rows.dailyUsage().get(bloodType);
            int currentStock = rows.currentStock().get(bloodType);
            totalDailyUsage += dailyUsage;
            totalCurrentStock += currentStock;
            bloodTypes.put(
                    bloodType,
                    metric(dailyUsage, currentStock, percentage)
            );
        }

        return new BloodStockStatusResponse.ProductStatus(
                name,
                unit,
                metric(totalDailyUsage, totalCurrentStock, percentage),
                bloodTypes
        );
    }

    private BloodStockStatusResponse.StockMetric metric(
            int dailyUsage,
            int currentStock,
            boolean percentage
    ) {
        if (dailyUsage <= 0) {
            throw new IllegalStateException("1일 소요량은 0보다 커야 합니다.");
        }

        double ratio = (double) currentStock / dailyUsage;
        double value = percentage
                ? Math.round(ratio * 10.0) * 10.0
                : Math.round(ratio * 10.0) / 10.0;

        return new BloodStockStatusResponse.StockMetric(
                dailyUsage,
                currentStock,
                value
        );
    }

    private BloodStockStatusResponse.CrisisStatus crisisStatus(double days) {
        if (days < 1.0) {
            return new BloodStockStatusResponse.CrisisStatus(
                    "심각", "1일분 미만", "즉각적인 혈액 수급 대응이 필요합니다!"
            );
        }
        if (days < 2.0) {
            return new BloodStockStatusResponse.CrisisStatus(
                    "경계", "2일분 미만", "혈액 수급이 매우 부족한 상태입니다!"
            );
        }
        if (days < 3.0) {
            return new BloodStockStatusResponse.CrisisStatus(
                    "주의", "3일분 미만", "혈액 수급에 각별한 주의가 필요합니다!"
            );
        }
        if (days < 5.0) {
            return new BloodStockStatusResponse.CrisisStatus(
                    "관심", "5일분 미만", "안정적인 혈액 수급을 위해 헌혈 참여가 필요합니다!"
            );
        }
        return new BloodStockStatusResponse.CrisisStatus(
                "적정", "5일분 이상", "적정 혈액 보유량을 유지하고 있습니다."
        );
    }

    private int parseNumber(String text) {
        String number = text == null ? "" : text.replaceAll("[^0-9]", "");
        if (number.isBlank()) {
            throw new IllegalStateException("숫자 형식의 혈액 수치를 찾지 못했습니다: " + text);
        }
        return Integer.parseInt(number);
    }

    private String previousHeadingText(Element table) {
        Element previous = table.previousElementSibling();
        while (previous != null) {
            if (List.of("h2", "h3", "h4").contains(previous.tagName().toLowerCase(Locale.ROOT))) {
                return normalize(previous.text());
            }
            previous = previous.previousElementSibling();
        }
        return "";
    }

    private String normalize(String text) {
        return text == null
                ? ""
                : text.replace('\u00A0', ' ')
                        .replaceAll("\\s+", "")
                        .toUpperCase(Locale.ROOT);
    }

    private record ProductRows(
            Map<String, Integer> dailyUsage,
            Map<String, Integer> currentStock
    ) {
    }
}
