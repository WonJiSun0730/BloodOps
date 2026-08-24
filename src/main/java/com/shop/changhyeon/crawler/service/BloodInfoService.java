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

import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloodInfoService {

    private static final String TARGET_URL =
            "https://www.bloodinfo.net/knrcbs/bi/info/bldHoldSttus.do?mi=1046";

    public static final Long NATIONAL_CENTER_ID = 0L;

    private static final List<String> BLOOD_TYPES =
            List.of("A", "B", "O", "AB");

    /*
     * 공식 페이지 화면명 -> 프로젝트 productType 매핑
     *
     * 적혈구제제       -> 전혈
     * 농축혈소판       -> 혈소판다종
     * 성분채혈혈소판   -> 혈소판단종
     */

    private final DummyStockGenerator dummyStockGenerator;

    @Value("${crawler.use-dummy:true}")
    private boolean useDummy;

    @Value("${crawler.timeout-ms:30000}")
    private int timeoutMs;

    @Value("${crawler.retry-count:3}")
    private int retryCount;

    @Value("${crawler.retry-delay-ms:2000}")
    private long retryDelayMs;


    /**
     * 기존 재고 조회용 메서드
     *
     * crawler.use-dummy=true
     * -> 더미 데이터 반환
     *
     * crawler.use-dummy=false
     * -> 공식 사이트에서 전혈(적혈구) 데이터 반환
     */
    public List<RawStockData> fetchStockData() {

        if (useDummy) {

            log.info("[크롤러] 더미 모드로 혈액보유현황을 생성합니다.");

            return dummyStockGenerator.generateDummyData();
        }

        return fetchOfficialStockData();
    }


    /**
     * 공식 페이지에서 전혈(적혈구제제) 데이터 4건만 반환
     */
    public List<RawStockData> fetchOfficialStockData() {

        return fetchDocumentAndParse()
                .stream()
                .filter(stock -> "전혈".equals(stock.getProductType()))
                .toList();
    }


    /**
     * 공식 페이지에서
     *
     * 전혈              4건
     * 혈소판다종        4건
     * 혈소판단종        4건
     *
     * 총 12건 반환
     */
    public List<RawStockData> fetchOfficialProductStockData() {

        return fetchDocumentAndParse();
    }


    /**
     * 실제 공식 사이트 다운로드 + HTML 파싱
     */
    private List<RawStockData> fetchDocumentAndParse() {

        log.info(
                "[크롤러] 대한적십자사 혈액보유현황 크롤링 시작 " +
                        "target={}, timeout={}ms, retryCount={}",
                TARGET_URL,
                timeoutMs,
                retryCount
        );

        try {

            Document doc = fetchDocumentWithRetry();

            List<RawStockData> result = parseProductStock(doc);

            validateProductResult(result);

            log.info(
                    "[크롤러] 제제별 혈액보유현황 크롤링 완료: {}건",
                    result.size()
            );

            return result;

        } catch (Exception e) {

            log.error(
                    "[크롤러] 대한적십자사 혈액보유현황 크롤링 실패",
                    e
            );

            throw new IllegalStateException(
                    "대한적십자사 혈액보유현황 크롤링에 실패했습니다.",
                    e
            );
        }
    }


    /**
     * 대한적십자사 서버가 일시적으로 느린 경우를 대비한 재시도
     */
    private Document fetchDocumentWithRetry() throws Exception {

        int maxAttempts = Math.max(1, retryCount);

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            try {

                log.info(
                        "[크롤러] HTTP 요청 시도 {}/{} - timeout={}ms",
                        attempt,
                        maxAttempts,
                        timeoutMs
                );

                Document document = Jsoup.connect(TARGET_URL)

                        /*
                         * 일반 브라우저와 유사한 User-Agent 사용
                         */
                        .userAgent(
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                        "Chrome/151.0.0.0 Safari/537.36"
                        )

                        /*
                         * 혈액관리본부 메인 페이지를 통해 들어온 요청처럼 설정
                         */
                        .referrer("https://www.bloodinfo.net/")

                        /*
                         * 일반적인 브라우저 요청 헤더
                         */
                        .header(
                                "Accept",
                                "text/html,application/xhtml+xml," +
                                        "application/xml;q=0.9," +
                                        "image/avif,image/webp,*/*;q=0.8"
                        )

                        .header(
                                "Accept-Language",
                                "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"
                        )

                        .header(
                                "Cache-Control",
                                "no-cache"
                        )

                        .header(
                                "Pragma",
                                "no-cache"
                        )

                        /*
                         * 리다이렉트 허용
                         */
                        .followRedirects(true)

                        /*
                         * HTTP 오류 상태도 확인 가능하도록 설정
                         */
                        .ignoreHttpErrors(false)

                        /*
                         * application.properties 값 사용
                         */
                        .timeout(timeoutMs)

                        .get();

                log.info(
                        "[크롤러] HTTP 요청 성공 - attempt={}",
                        attempt
                );

                return document;

            } catch (SocketTimeoutException e) {

                lastException = e;

                log.warn(
                        "[크롤러] HTTP 요청 타임아웃 {}/{} - timeout={}ms",
                        attempt,
                        maxAttempts,
                        timeoutMs
                );

                if (attempt < maxAttempts) {

                    log.info(
                            "[크롤러] {}ms 후 재시도합니다.",
                            retryDelayMs
                    );

                    Thread.sleep(retryDelayMs);
                }

            } catch (Exception e) {

                lastException = e;

                log.warn(
                        "[크롤러] HTTP 요청 실패 {}/{} - {}",
                        attempt,
                        maxAttempts,
                        e.getMessage()
                );

                if (attempt < maxAttempts) {

                    Thread.sleep(retryDelayMs);
                }
            }
        }

        if (lastException != null) {
            throw lastException;
        }

        throw new IllegalStateException(
                "대한적십자사 페이지 요청에 실패했습니다."
        );
    }


    /**
     * 모든 table을 순회하면서
     * 적혈구 / 농축혈소판 / 성분채혈혈소판 테이블 찾기
     */
    private List<RawStockData> parseProductStock(Document document) {

        List<RawStockData> result = new ArrayList<>();

        Elements tables = document.select("table");

        log.info("[크롤러] HTML 전체 table 개수 = {}", tables.size());

        int tableIndex = 0;

        for (Element table : tables) {

            tableIndex++;

            String normalizedText = normalize(table.text());

            log.info(
                    "[크롤러][TABLE-{}] {}",
                    tableIndex,
                    normalizedText.length() > 300
                            ? normalizedText.substring(0, 300)
                            : normalizedText
            );

            /*
             * 1. 실제 사이트의 혈소판 복합 테이블 처리
             *
             * 농축혈소판 + 성분채혈혈소판이 하나의 table 안에 들어 있음
             */
            if (normalizedText.contains("농축혈소판")
                    && normalizedText.contains("성분채혈혈소판")) {

                log.info(
                        "[크롤러][TABLE-{}] 혈소판 복합 테이블 감지",
                        tableIndex
                );

                List<RawStockData> plateletStocks =
                        parseCombinedPlateletTable(table);

                log.info(
                        "[크롤러][TABLE-{}] 혈소판 파싱 결과={}건",
                        tableIndex,
                        plateletStocks.size()
                );

                result.addAll(plateletStocks);

                continue;
            }


            /*
             * 2. 일반 단일 제제 테이블
             */
            String productType = detectProductType(table);

            log.info(
                    "[크롤러][TABLE-{}] 감지 productType={}",
                    tableIndex,
                    productType
            );

            if (productType == null) {
                continue;
            }

            List<RawStockData> stock =
                    parseStockTable(
                            table,
                            productType
                    );

            log.info(
                    "[크롤러][TABLE-{}] 파싱 결과={}건",
                    tableIndex,
                    stock.size()
            );

            if (stock.size() == 4) {
                result.addAll(stock);
            }
        }

        log.info(
                "[크롤러] 최종 parseProductStock 결과={}건",
                result.size()
        );

        return result;
    }

    private List<RawStockData> parseCombinedPlateletTable(Element table) {

        List<RawStockData> result = new ArrayList<>();

        Elements rows = table.select("tr");

        if (rows.isEmpty()) {
            return List.of();
        }

        /*
         * A/B/O/AB 컬럼 위치
         */
        Map<String, Integer> colMap =
                findBloodTypeColumns(rows);

        if (colMap.size() < 4) {

            log.warn(
                    "[크롤러] 혈소판 테이블에서 혈액형 컬럼을 찾지 못했습니다. colMap={}",
                    colMap
            );

            return List.of();
        }


        String currentProductType = null;

        for (Element row : rows) {

            String rowText =
                    normalize(row.text());

            /*
             * 농축혈소판 시작
             */
            if (rowText.contains("농축혈소판")) {

                currentProductType =
                        "혈소판다종";

                log.info(
                        "[크롤러] 농축혈소판 영역 감지"
                );
            }


            /*
             * 성분채혈혈소판 시작
             */
            if (rowText.contains("성분채혈혈소판")) {

                currentProductType =
                        "혈소판단종";

                log.info(
                        "[크롤러] 성분채혈혈소판 영역 감지"
                );
            }


            /*
             * 아직 어떤 제제인지 모르면 넘어감
             */
            if (currentProductType == null) {
                continue;
            }


            /*
             * 현재 혈액보유량 행만 사용
             *
             * 1일 소요량, 보유율은 제외
             */
            if (!rowText.contains("현재혈액보유량")
                    && !rowText.contains("현재보유량")) {

                continue;
            }


            Elements cells =
                    row.select("th, td");

            List<RawStockData> currentStocks =
                    new ArrayList<>();


            for (Map.Entry<String, Integer> entry :
                    colMap.entrySet()) {

                int index =
                        entry.getValue();

                if (index >= cells.size()) {
                    continue;
                }

                Integer quantity =
                        parseQuantity(
                                cells.get(index).text()
                        );

                if (quantity != null) {

                    currentStocks.add(
                            new RawStockData(
                                    NATIONAL_CENTER_ID,
                                    entry.getKey(),
                                    quantity,
                                    currentProductType
                            )
                    );
                }
            }


            if (currentStocks.size() == 4) {

                result.addAll(
                        currentStocks
                );

                log.info(
                        "[크롤러] {} 현재보유량 파싱 성공: {}",
                        currentProductType,
                        currentStocks
                );
            }
        }


        /*
         * 혈소판 2종 × 혈액형 4종 = 총 8건
         */
        if (result.size() != 8) {

            log.warn(
                    "[크롤러] 혈소판 파싱 결과가 8건이 아닙니다. count={}",
                    result.size()
            );
        }

        return result;
    }


    /**
     * table이 어떤 혈액제제인지 판별
     *
     * 1. caption 확인
     * 2. caption이 없으면 이전 h2/h3/h4 확인
     *
     * 테스트 fixture와 실제 사이트 둘 다 대응
     */
    private String detectProductType(Element table) {

        /*
         * 1차: caption
         */
        Element caption = table.selectFirst("caption");

        if (caption != null) {

            String text = normalize(caption.text());

            if (text.contains("적혈구제제")) {
                return "전혈";
            }

            if (text.contains("농축혈소판")) {
                return "혈소판다종";
            }

            if (text.contains("성분채혈혈소판")) {
                return "혈소판단종";
            }
        }


        /*
         * 2차: table 앞 제목 탐색
         */
        Element previous = table.previousElementSibling();

        while (previous != null) {

            String tagName = previous.tagName();

            if ("h2".equalsIgnoreCase(tagName)
                    || "h3".equalsIgnoreCase(tagName)
                    || "h4".equalsIgnoreCase(tagName)) {

                String text = normalize(previous.text());

                if (text.contains("적혈구제제")) {
                    return "전혈";
                }

                if (text.contains("농축혈소판")) {
                    return "혈소판다종";
                }

                if (text.contains("성분채혈혈소판")) {
                    return "혈소판단종";
                }
            }

            previous = previous.previousElementSibling();
        }

        return null;
    }


    /**
     * 한 개 제제 테이블에서
     *
     * A
     * B
     * O
     * AB
     *
     * 현재 혈액보유량 4건 생성
     */
    private List<RawStockData> parseStockTable(
            Element table,
            String productType
    ) {

        Elements rows = table.select("tr");

        if (rows.isEmpty()) {
            return List.of();
        }


        /*
         * A/B/O/AB 컬럼 위치 찾기
         */
        Map<String, Integer> colMap =
                findBloodTypeColumns(rows);

        if (colMap.size() < 4) {
            return List.of();
        }


        /*
         * "현재 혈액보유량" 행만 선택
         *
         * 1일 소요량과 보유상태는 제외
         */
        Element targetRow =
                findCurrentStockRow(rows);

        if (targetRow == null) {
            return List.of();
        }


        Elements cells =
                targetRow.select("th, td");

        List<RawStockData> result =
                new ArrayList<>();


        for (Map.Entry<String, Integer> entry :
                colMap.entrySet()) {

            int index = entry.getValue();

            if (index >= cells.size()) {
                continue;
            }

            Integer quantity =
                    parseQuantity(cells.get(index).text());

            if (quantity != null) {

                result.add(
                        new RawStockData(
                                NATIONAL_CENTER_ID,
                                entry.getKey(),
                                quantity,
                                productType
                        )
                );
            }
        }

        /*
         * A/B/O/AB 4개가 완전하지 않으면 사용하지 않음
         */
        return result.size() == 4
                ? result
                : List.of();
    }


    /**
     * 예전 전혈 전용 parser 호환용
     */
    private List<RawStockData> parseBloodStock(
            Document document
    ) {

        Elements tables =
                document.select("table");

        if (tables.isEmpty()) {
            return List.of();
        }

        for (Element table : tables) {

            if (isBloodStockTable(table)) {

                List<RawStockData> result =
                        parseStockTable(
                                table,
                                "전혈"
                        );

                if (!result.isEmpty()) {
                    return result;
                }
            }
        }

        return List.of();
    }


    /**
     * 일반적인 혈액 보유량 테이블인지 확인
     */
    private boolean isBloodStockTable(Element table) {

        String text =
                normalize(table.text());

        boolean hasBloodType =
                BLOOD_TYPES
                        .stream()
                        .anyMatch(
                                type ->
                                        text.contains(type + "형")
                                                || text.contains(type)
                        );

        boolean hasCurrentStock =
                text.contains("현재혈액보유량")
                        || text.contains("현재보유량")
                        || text.contains("혈액보유량");

        return hasBloodType
                && hasCurrentStock;
    }


    /**
     * 예전 호출 호환
     */
    private List<RawStockData> parseStockTable(
            Element table
    ) {

        return parseStockTable(
                table,
                "전혈"
        );
    }


    /**
     * A형/B형/O형/AB형 컬럼 위치 찾기
     */
    private Map<String, Integer> findBloodTypeColumns(
            Elements rows
    ) {

        Map<String, Integer> result =
                new LinkedHashMap<>();

        for (Element row : rows) {

            Elements cells =
                    row.select("th, td");

            for (int i = 0; i < cells.size(); i++) {

                String bloodType =
                        parseBloodType(
                                cells.get(i).text()
                        );

                if (bloodType != null) {

                    result.putIfAbsent(
                            bloodType,
                            i
                    );
                }
            }

            if (result.size() == 4) {
                break;
            }
        }

        return result;
    }


    /**
     * 현재 혈액보유량 행 찾기
     *
     * 1일 소요량 / 보유상태 제외
     */
    private Element findCurrentStockRow(
            Elements rows
    ) {

        return rows
                .stream()
                .filter(row -> {

                    String text =
                            normalize(row.text());

                    return text.contains("현재혈액보유량")
                            || text.contains("현재보유량");
                })
                .findFirst()
                .orElse(null);
    }


    /**
     * HTML 컬럼명을 프로젝트 혈액형 문자열로 변환
     */
    private String parseBloodType(
            String text
    ) {

        String value =
                normalize(text);

        return switch (value) {

            case "A", "A형" -> "A";

            case "B", "B형" -> "B";

            case "O", "O형" -> "O";

            case "AB", "AB형" -> "AB";

            default -> null;
        };
    }


    /**
     * "5,038"
     * "5038 Unit"
     *
     * 등을 Integer로 변환
     */
    private Integer parseQuantity(
            String text
    ) {

        if (text == null
                || text.isBlank()) {

            return null;
        }

        String value =
                text
                        .replace(",", "")
                        .replaceAll(
                                "[^0-9-]",
                                ""
                        );

        if (value.isBlank()
                || "-".equals(value)) {

            return null;
        }

        try {

            int quantity =
                    Integer.parseInt(value);

            return quantity >= 0
                    ? quantity
                    : null;

        } catch (NumberFormatException e) {

            return null;
        }
    }


    /**
     * 공백/대소문자 차이를 없애서 HTML 비교를 안정화
     */
    private String normalize(
            String text
    ) {

        return text == null
                ? ""
                : text
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
    }


    /**
     * 전혈 4건 검증
     */
    private void validateResult(
            List<RawStockData> result
    ) {

        if (result.size() != 4) {

            throw new IllegalStateException(
                    "혈액보유현황 데이터가 4건이 아닙니다. count="
                            + result.size()
            );
        }


        long distinctTypes =
                result
                        .stream()
                        .peek(stock -> {

                            if (!NATIONAL_CENTER_ID.equals(
                                    stock.getCenterId()
                            )
                                    || stock.getQuantity() == null
                                    || stock.getQuantity() < 0) {

                                throw new IllegalStateException(
                                        "유효하지 않은 혈액보유 데이터 존재"
                                );
                            }
                        })
                        .map(
                                RawStockData::getBloodType
                        )
                        .distinct()
                        .count();


        if (distinctTypes != 4) {

            throw new IllegalStateException(
                    "혈액형 중복 또는 누락이 존재합니다."
            );
        }
    }


    /**
     * 전혈 + 혈소판단종 + 혈소판다종
     *
     * 3제제 × 4혈액형 = 12건 검증
     */
    private void validateProductResult(
            List<RawStockData> result
    ) {

        if (result.size() != 12) {

            throw new IllegalStateException(
                    "제제별 혈액보유현황 데이터가 12건이 아닙니다. count="
                            + result.size()
            );
        }


        Map<String, Long> productCounts =
                result
                        .stream()
                        .peek(stock -> {

                            if (!NATIONAL_CENTER_ID.equals(
                                    stock.getCenterId()
                            )
                                    || stock.getQuantity() == null
                                    || stock.getQuantity() < 0) {

                                throw new IllegalStateException(
                                        "유효하지 않은 혈액보유 데이터 존재"
                                );
                            }
                        })
                        .collect(
                                Collectors.groupingBy(
                                        RawStockData::getProductType,
                                        Collectors.counting()
                                )
                        );


        for (String product :
                List.of(
                        "전혈",
                        "혈소판단종",
                        "혈소판다종"
                )) {

            if (!Long.valueOf(4L)
                    .equals(
                            productCounts.get(product)
                    )) {

                throw new IllegalStateException(
                        "제제별 혈액형 데이터가 누락되었습니다: "
                                + product
                );
            }
        }
    }
}