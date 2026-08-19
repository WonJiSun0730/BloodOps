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
import java.util.List;

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

    /**
     * 외부에서 호출하여 파싱된 Document 객체를 얻는 메서드
     */
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

    // 적혈구제제 BloodStock 인스턴스 생성(파싱)
    public BloodStock parseBloodStock(Document document, String titleName, String targetType) {
        Element title = findTitle(document, titleName);
        Element table = findNextTable(title);
        return parseBloodStockFromTable(table, targetType);
    }

    // 혈소판제제 PlateletStock 인스턴스 생성(파싱)
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

        int[] daily = extractNumbers(dailyRow);
        int[] current = extractNumbers(currentRow);

        return new BloodStock(
                daily[0], daily[1], daily[2], daily[3], daily[4],
                current[0], current[1], current[2], current[3], current[4]
        );
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

    private int[] extractNumbers(Element row) {
        List<Integer> numbers = new ArrayList<>();

        for (Element cell : row.select("td")) {
            String text = cell.text().trim();

            if (text.contains("소요량") || text.contains("보유량")) {
                continue;
            }

            String cleaned = text.replaceAll("[^0-9]", "");
            if (!cleaned.isEmpty()) {
                numbers.add(Integer.parseInt(cleaned));
            }
        }

        int[] result = new int[5];

        if (numbers.size() == 5) {
            for (int i = 0; i < 5; i++) {
                result[i] = numbers.get(i);
            }
        } else if (numbers.size() == 4) {
            result[0] = -1; // 웹 표에 합계 셀이 없는 경우
            result[1] = numbers.get(0);
            result[2] = numbers.get(1);
            result[3] = numbers.get(2);
            result[4] = numbers.get(3);
        } else {
            throw new RuntimeException("데이터 셀 개수 파싱 에러 (개수: " + numbers.size() + ") - " + row.text());
        }

        return result;
    }

    // DTO 객체 (Record)
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