package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloodInfoService {

    private static final String URL = "https://www.bloodinfo.net/knrcbs/bi/info/bldHoldSttus.do?mi=1046";
    private final HttpClient client;

    public BloodInfoService() {
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public BloodInfoService(HttpClient client) {
        this.client = client;
    }

    public Document fetchDocument() {
        LocalDate today = LocalDate.now();
        String formData = String.format("selRbcYear=%d&selRbcMon=%d&selRbcDay=%d&selRbcTerm=1",
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("User-Agent", "Mozilla/5.0")
                .header("Referer", URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                throw new RuntimeException("혈액보유현황 조회 실패: HTTP " + response.statusCode());
            }

            return Jsoup.parse(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("혈액보유현황 데이터를 가져오는 중 오류가 발생했습니다.", e);
        }
    }

    public BloodStock parseBloodStock(Document document, String titleName, String targetType) {
        Element title = findTitle(document, titleName);
        Element table = findNextTable(title);
        return parseBloodStockFromTable(table, targetType);
    }

    public PlateletStock parsePlatelets(Document document) {
        Element title = findTitle(document, "혈소판제제 보유 현황");
        Element table = findNextTable(title);

        BloodStock concentrated = parseBloodStockFromTable(table, "농축혈소판");
        BloodStock apheresis = parseBloodStockFromTable(table, "성분채혈혈소판");

        return new PlateletStock(concentrated, apheresis);
    }

    private BloodStock parseBloodStockFromTable(Element table, String typePrefix) {
        Elements rows = table.select("tbody tr");
        Element dailyRow = null;
        Element currentRow = null;
        boolean isTargetSection = (typePrefix == null);

        for (Element row : rows) {
            String text = row.text().trim();
            if (text.isEmpty()) continue;

            if (typePrefix != null) {
                if (text.startsWith(typePrefix)) {
                    isTargetSection = true;
                } else if (isTargetSection && text.matches("^(농축혈소판|성분채혈혈소판).*") && !text.startsWith(typePrefix)) {
                    break;
                }
            }

            if (!isTargetSection) continue;

            if (text.contains("1일 소요량")) dailyRow = row;
            if (text.contains("현재 혈액보유량")) currentRow = row;

            if (dailyRow != null && currentRow != null) break;
        }

        if (dailyRow == null || currentRow == null) {
            throw new RuntimeException((typePrefix != null ? typePrefix : "해당") + " 데이터를 찾을 수 없습니다.");
        }

        // 동적으로 정확한 컬럼 위치 파악
        Map<String, Integer> colMap = getHeaderColumnMap(table);

        int[] daily = extractNumbersWithMap(dailyRow, colMap);
        int[] current = extractNumbersWithMap(currentRow, colMap);

        return new BloodStock(
                daily[0], daily[1], daily[2], daily[3], daily[4],
                current[0], current[1], current[2], current[3], current[4]
        );
    }

    // [버그 수정] AB형 우선 체크 및 헤더 행 정밀 타겟팅
    private Map<String, Integer> getHeaderColumnMap(Element table) {
        Map<String, Integer> map = new HashMap<>();

        // 혈액형 헤더("A형", "B형", "O형", "AB형")가 들어 있는 tr만 선별
        Elements headerRows = table.select("thead tr, tbody tr");
        Element targetHeaderRow = null;

        for (Element tr : headerRows) {
            String text = tr.text();
            if (text.contains("A형") && text.contains("AB형")) {
                targetHeaderRow = tr;
                break;
            }
        }

        if (targetHeaderRow == null) {
            // 헤더를 못 찾은 경우 기본순서 (0:합계, 1:A, 2:B, 3:O, 4:AB) 세팅
            map.put("TOTAL", 0); map.put("A", 1); map.put("B", 2); map.put("O", 3); map.put("AB", 4);
            return map;
        }

        int dataColIndex = 0;
        for (Element header : targetHeaderRow.select("th")) {
            String text = header.text().trim();

            if (text.contains("구분") || text.contains("제제")) continue;

            if (text.contains("합계")) {
                map.put("TOTAL", dataColIndex++);
            } else if (text.contains("AB형") || text.equals("AB")) { // [중요] AB형을 A형보다 먼저 처리
                map.put("AB", dataColIndex++);
            } else if (text.contains("A형") || text.equals("A")) {
                map.put("A", dataColIndex++);
            } else if (text.contains("B형") || text.equals("B")) {
                map.put("B", dataColIndex++);
            } else if (text.contains("O형") || text.equals("O")) {
                map.put("O", dataColIndex++);
            }
        }
        return map;
    }

    private int[] extractNumbersWithMap(Element row, Map<String, Integer> colMap) {
        List<Integer> numbers = new ArrayList<>();

        for (Element cell : row.select("td")) {
            String text = cell.text().trim();
            if (text.contains("소요량") || text.contains("보유량")) continue;

            String cleaned = text.replaceAll("[^0-9]", "");
            if (!cleaned.isEmpty()) {
                numbers.add(Integer.parseInt(cleaned));
            }
        }

        int[] result = new int[5]; // [0:합계, 1:A, 2:B, 3:O, 4:AB]

        if (colMap.containsKey("A") && colMap.get("A") < numbers.size()) result[1] = numbers.get(colMap.get("A"));
        if (colMap.containsKey("B") && colMap.get("B") < numbers.size()) result[2] = numbers.get(colMap.get("B"));
        if (colMap.containsKey("O") && colMap.get("O") < numbers.size()) result[3] = numbers.get(colMap.get("O"));
        if (colMap.containsKey("AB") && colMap.get("AB") < numbers.size()) result[4] = numbers.get(colMap.get("AB"));

        if (colMap.containsKey("TOTAL") && colMap.get("TOTAL") < numbers.size()) {
            result[0] = numbers.get(colMap.get("TOTAL"));
        } else {
            result[0] = result[1] + result[2] + result[3] + result[4];
        }

        return result;
    }

    private Element findTitle(Document document, String title) {
        return document.select("h3.tit1").stream()
                .filter(e -> e.text().contains(title))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(title + " 제목을 찾을 수 없습니다."));
    }

    private Element findNextTable(Element title) {
        Element current = title.nextElementSibling();
        while (current != null) {
            Element table = current.select("table").first();
            if (table != null) return table;
            current = current.nextElementSibling();
        }
        throw new RuntimeException("테이블을 찾을 수 없습니다.");
    }

    public record BloodStock(
            int dailyTotal, int dailyA, int dailyB, int dailyO, int dailyAB,
            int total, int a, int b, int o, int ab
    ) {}

    public record PlateletStock(
            BloodStock concentrated,
            BloodStock apheresis
    ) {}
}

//  적혈구제제 BloodStock 인스턴스 생성(파싱)
//  BloodInfoClient.BloodStock redBloodCell = parseBloodStock(document, "적혈구제제 보유 현황", null);
//
//  혈소판제제 PlateletStock 인스턴스 생성(파싱)
//  BloodInfoClient.PlateletStock platelet = parsePlatelets(document);
//
//  PlateletStock 내부의 BloodStock 인스턴스 각각 꺼내기
//  BloodInfoClient.BloodStock concentrated = platelet.concentrated(); // 농축혈소판
//  BloodInfoClient.BloodStock apheresis = platelet.apheresis();       // 성분채혈혈소판
//
//1일 소요량 데이터
//
//stock.dailyTotal() : 1일 소요량 합계 (웹 표에 합계 셀이 없는 경우 -1 반환)
//stock.dailyA() : 1일 소요량 A형
//stock.dailyB() : 1일 소요량 B형
//stock.dailyO() : 1일 소요량 O형
//stock.dailyAB() : 1일 소요량 AB형
//
//현재 혈액보유량 데이터
//
//stock.total() : 현재 혈액보유량 합계
//stock.a() : 현재 혈액보유량 A형
//stock.b() : 현재 혈액보유량 B형
//stock.o() : 현재 혈액보유량 O형
//stock.ab() : 현재 혈액보유량 AB형

//사용 예시
//redBloodCell.dailyTotal(); // 적혈구 1일 소요량 합계
//concentrated.total();   // 농축혈소판 현재 보유량 합계
//apheresis.ab();   // 성분채혈혈소판 현재 AB형 보유량