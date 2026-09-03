package com.shop.changhyeon.crawler.service;

import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.changhyeon.crawler.model.RawStockData;
import com.shop.changhyeon.management.entity.BloodUnit;
import com.shop.changhyeon.management.repository.BloodUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyBloodDummyService {

    /**
     * 자동 생성 더미 혈액팩 식별용 Prefix
     */
    private static final String DUMMY_UNIT_PREFIX = "DUMMY-BLD-";

    /**
     * 한국 시간
     */
    private static final ZoneId KST =
            ZoneId.of("Asia/Seoul");

    /**
     * unitId 날짜 포맷
     *
     * 예:
     * 20260825
     */
    private static final DateTimeFormatter UNIT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * DB 저장 배치 크기
     */
    private static final int SAVE_BATCH_SIZE = 1000;

    /**
     * 진행률 출력 간격
     *
     * 5% 단위
     */
    private static final int PROGRESS_STEP = 5;

    private final BloodInfoService bloodInfoService;
    private final BloodUnitRepository bloodUnitRepository;
    private final CenterRepository centerRepository;

    /**
     * application.properties
     *
     * crawler.dummy-generation.enabled=true
     */
    @Value("${crawler.dummy-generation.enabled:true}")
    private boolean enabled;


    /**
     * 서버 실행 완료 후 자동 생성
     */
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void generateOnStartup() {

        try {

            generateTodayIfNeeded();

        } catch (Exception e) {

            log.error(
                    "[더미 생성] 시작 시 혈액정보 크롤링 또는 더미 생성 실패 - 서버는 계속 실행합니다.",
                    e
            );
        }
    }


    /**
     * 매일 00:10 KST 자동 생성
     */
    @Transactional
    @Scheduled(
            cron = "${crawler.dummy-generation.cron:0 10 0 * * *}",
            zone = "Asia/Seoul"
    )
    public void generateDaily() {

        generateTodayIfNeeded();
    }


    /**
     * 오늘 데이터가 없다면 생성
     */
    @Transactional
    public void generateTodayIfNeeded() {

        if (!enabled) {

            log.info(
                    "[더미 생성] 자동 더미 생성 기능이 비활성화되어 있습니다."
            );

            return;
        }


        LocalDate today =
                LocalDate.now(KST);


        String todayPrefix =
                createTodayDummyPrefix(today);


        /*
         * 오늘 생성된 더미가 이미 존재하면
         * 중복 생성하지 않는다.
         */
        boolean alreadyGenerated =
                bloodUnitRepository
                        .existsByUnitIdStartingWith(todayPrefix);


        if (alreadyGenerated) {

            assignExistingUnassignedUnits(today);

            log.info(
                    "[더미 생성] 오늘({}) 데이터가 이미 존재하여 신규 생성과 재배분을 생략합니다.",
                    today
            );

            return;
        }


        log.info(
                "============================================================"
        );

        log.info(
                "[더미 생성] 오늘({}) 공식 혈액보유현황 기반 생성을 시작합니다.",
                today
        );

        log.info(
                "============================================================"
        );


        /*
         * 공식 혈액보유현황 크롤링
         *
         * 전혈
         * 혈소판다종
         * 혈소판단종
         */
        List<RawStockData> officialStocks =
                bloodInfoService
                        .fetchOfficialProductStockData();


        /*
         * Map 구조 변환
         *
         * productType
         *     -> bloodType
         *         -> quantity
         */
        Map<String, Map<String, Integer>> stockMap =
                toStockMap(officialStocks);


        /*
         * 혈장 생성
         *
         * 전혈 × 1.2
         */
        addPlasmaStock(stockMap);


        /*
         * 기존 자동 생성 더미 제거
         */
        int deletedCount =
                bloodUnitRepository
                        .deleteByUnitIdPrefix(
                                DUMMY_UNIT_PREFIX
                        );


        log.info(
                "[더미 생성] 기존 자동 생성 더미 삭제 완료: {}건",
                deletedCount
        );


        /*
         * BloodUnit 생성
         */
        List<BloodUnit> units =
                createUnits(
                        stockMap,
                        today
                );

        assignCentersEvenly(units, today);

        log.info(
                "[더미 생성] 생성 대상 데이터: 총 {}건",
                units.size()
        );


        /*
         * DB 저장
         *
         * 1000건씩 저장하며
         * 5% 단위 진행률 출력
         */
        saveUnitsWithProgress(units);


        log.info(
                "============================================================"
        );

        log.info(
                "[더미 생성] 오늘({}) 더미 데이터 생성 완료: 총 {}건",
                today,
                units.size()
        );

        log.info(
                "============================================================"
        );


        /*
         * 최종 제제별 수량 로그
         */
        logStockSummary(
                stockMap,
                units.size()
        );
    }


    /**
     * RawStockData -> Map 변환
     */
    private Map<String, Map<String, Integer>> toStockMap(
            List<RawStockData> stocks
    ) {

        Map<String, Map<String, Integer>> result =
                new LinkedHashMap<>();


        for (RawStockData stock : stocks) {

            if (stock == null) {
                continue;
            }


            String productType =
                    stock.getProductType();

            String bloodType =
                    stock.getBloodType();

            int quantity =
                    stock.getQuantity();


            if (productType == null
                    || productType.isBlank()
                    || bloodType == null
                    || bloodType.isBlank()) {

                log.warn(
                        "[더미 생성] 잘못된 크롤링 데이터 제외: productType={}, bloodType={}",
                        productType,
                        bloodType
                );

                continue;
            }


            /*
             * 음수 방지
             */
            quantity =
                    Math.max(
                            0,
                            quantity
                    );


            result
                    .computeIfAbsent(
                            productType,
                            key -> new LinkedHashMap<>()
                    )
                    .put(
                            bloodType,
                            quantity
                    );
        }


        return result;
    }


    /**
     * 혈장 더미 생성
     *
     * 전혈 보유량 × 1.2
     */
    private void addPlasmaStock(
            Map<String, Map<String, Integer>> stockMap
    ) {

        Map<String, Integer> wholeBlood =
                stockMap.get("전혈");


        if (wholeBlood == null
                || wholeBlood.size() != 4) {

            throw new IllegalStateException(
                    "전혈 공식 수치가 없어 혈장 더미 데이터를 생성할 수 없습니다."
            );
        }


        Map<String, Integer> plasma =
                new LinkedHashMap<>();


        for (Map.Entry<String, Integer> entry
                : wholeBlood.entrySet()) {

            String bloodType =
                    entry.getKey();

            int quantity =
                    entry.getValue();


            int plasmaQuantity =
                    Math.round(
                            quantity * 1.2f
                    );


            plasma.put(
                    bloodType,
                    plasmaQuantity
            );
        }


        stockMap.put(
                "혈장",
                plasma
        );
    }


    /**
     * 전체 BloodUnit 생성
     */
    private List<BloodUnit> createUnits(
            Map<String, Map<String, Integer>> stockMap,
            LocalDate generationDate
    ) {

        List<BloodUnit> result =
                new ArrayList<>();


        AtomicLong sequence =
                new AtomicLong(1);


        /*
         * 생성 순서
         */
        List<String> productOrder =
                List.of(
                        "전혈",
                        "혈장",
                        "혈소판단종",
                        "혈소판다종"
                );


        /*
         * 혈액형 순서
         */
        List<String> bloodTypeOrder =
                List.of(
                        "A",
                        "B",
                        "O",
                        "AB"
                );


        for (String productType : productOrder) {

            Map<String, Integer> productStock =
                    stockMap.get(productType);


            if (productStock == null) {

                throw new IllegalStateException(
                        "제제 수치가 누락되었습니다: "
                                + productType
                );
            }


            int expirationDays =
                    expirationDays(productType);


            for (String bloodType : bloodTypeOrder) {

                Integer quantity =
                        productStock.get(bloodType);


                if (quantity == null) {

                    throw new IllegalStateException(
                            productType
                                    + "의 "
                                    + bloodType
                                    + "형 수치가 없습니다."
                    );
                }


                for (int i = 0; i < quantity; i++) {

                    long currentSequence =
                            sequence.getAndIncrement();


                    BloodUnit unit =
                            createUnit(
                                    productType,
                                    bloodType,
                                    generationDate,
                                    expirationDays,
                                    currentSequence
                            );


                    result.add(unit);
                }
            }
        }


        return result;
    }


    /**
     * BloodUnit 한 건 생성
     */
    private BloodUnit createUnit(
            String productType,
            String bloodType,
            LocalDate generationDate,
            int expirationDays,
            long sequence
    ) {

        LocalDateTime now =
                LocalDateTime.now(KST);


        /*
         * 채혈 시간을 일정 범위 안에서 분산
         */
        long maxHours =
                Math.max(
                        1L,
                        expirationDays * 24L
                );


        LocalDateTime collectionDate =
                now.minusHours(
                        sequence % maxHours
                );


        BloodUnit unit =
                new BloodUnit();


        /*
         * 헌혈자 ID
         */
        unit.setDonorId(
                createDonorId(
                        generationDate,
                        sequence
                )
        );


        /*
         * ABO 혈액형
         */
        unit.setBloodType(
                bloodType
        );


        /*
         * Rh 비율
         *
         * 약 90% Rh+
         * 약 10% Rh-
         */
        unit.setRhType(
                sequence % 10 == 0
                        ? "Rh-"
                        : "Rh+"
        );


        /*
         * 성별
         */
        unit.setGender(
                sequence % 2 == 0
                        ? "남"
                        : "여"
        );


        /*
         * 생년월일
         *
         * 약 20~65세 범위
         */
        unit.setBirthDate(
                generationDate
                        .minusYears(
                                20 + sequence % 46
                        )
                        .minusDays(
                                sequence % 365
                        )
        );


        /*
         * 혈액팩 ID
         *
         * DUMMY-BLD-20260825-00000001
         */
        unit.setUnitId(
                createUnitId(
                        generationDate,
                        sequence
                )
        );


        /*
         * 제제 종류
         */
        unit.setProductType(
                productType
        );


        /*
         * 채혈 일시
         */
        unit.setCollectionDate(
                collectionDate
        );


        /*
         * 유효기간
         */
        unit.setExpirationDate(
                collectionDate.plusDays(
                        expirationDays
                )
        );


        /*
         * 현재 처리 상태
         */
        unit.setStatus(
                "보유중"
        );


        return unit;
    }


    /**
     * center_id 도입 전에 생성된 기존 혈액팩을 전체 센터에 배정합니다.
     */
    private void assignExistingUnassignedUnits(
            LocalDate generationDate
    ) {
        List<BloodUnit> unassignedUnits =
                bloodUnitRepository.findAllByCenterIsNull();

        if (unassignedUnits.isEmpty()) {
            return;
        }

        assignCentersEvenly(unassignedUnits, generationDate);
        saveUnitsWithProgress(unassignedUnits);

        log.info(
                "[더미 생성] 기존 센터 미배정 혈액 재고 {}건 배정 완료",
                unassignedUnits.size()
        );
    }


    /**
     * 제제 + ABO + Rh 그룹별로 모든 센터에 균등 배분합니다.
     * 그룹마다 시작 센터를 변경해 남는 혈액팩이 특정 센터에 집중되지 않게 합니다.
     */
    private void assignCentersEvenly(
            List<BloodUnit> units,
            LocalDate generationDate
    ) {
        List<Center> centers = new ArrayList<>(
                centerRepository.findAll()
        );
        centers.sort(Comparator.comparing(Center::getId));

        if (centers.isEmpty()) {
            throw new IllegalStateException(
                    "혈액 재고를 배정할 센터가 없습니다."
            );
        }

        Map<StockGroupKey, List<BloodUnit>> groupedUnits =
                new LinkedHashMap<>();

        for (BloodUnit unit : units) {
            StockGroupKey key = new StockGroupKey(
                    unit.getProductType(),
                    unit.getBloodType(),
                    unit.getRhType()
            );
            groupedUnits
                    .computeIfAbsent(key, ignored -> new ArrayList<>())
                    .add(unit);
        }

        long seed = generationDate.toEpochDay();

        for (Map.Entry<StockGroupKey, List<BloodUnit>> entry
                : groupedUnits.entrySet()) {
            StockGroupKey key = entry.getKey();
            List<BloodUnit> group = entry.getValue();
            Random random = new Random(seed ^ key.hashCode());
            int startIndex = random.nextInt(centers.size());

            for (int index = 0; index < group.size(); index++) {
                Center center = centers.get(
                        (startIndex + index) % centers.size()
                );
                group.get(index).assignCenter(center);
            }
        }

        log.info(
                "[더미 생성] 전체 센터 {}곳에 제제·ABO·Rh 그룹별 균등 배분 완료",
                centers.size()
        );
    }

    private record StockGroupKey(
            String productType,
            String bloodType,
            String rhType
    ) {
    }


    /**
     * ================================
     * DB 저장 + 진행률 표시
     * ================================
     *
     * 1000건 단위 저장
     * 5% 단위 로그 출력
     */
    private void saveUnitsWithProgress(
            List<BloodUnit> units
    ) {

        if (units == null
                || units.isEmpty()) {

            log.info(
                    "[더미 생성] 저장할 데이터가 없습니다."
            );

            return;
        }


        int total =
                units.size();


        int lastDisplayedPercent =
                -1;


        log.info(
                "------------------------------------------------------------"
        );

        log.info(
                "[더미 생성] DB 저장 시작 - 총 {}건",
                total
        );

        /*
         * 시작 상태
         */
        log.info(
                "[더미 생성] [  0%] 저장 중... 0/{}건",
                total
        );

        lastDisplayedPercent =
                0;


        for (int start = 0;
             start < total;
             start += SAVE_BATCH_SIZE) {


            int end =
                    Math.min(
                            start + SAVE_BATCH_SIZE,
                            total
                    );


            List<BloodUnit> batch =
                    units.subList(
                            start,
                            end
                    );


            /*
             * 1000건 단위 저장
             */
            bloodUnitRepository
                    .saveAll(batch);


            /*
             * DB 즉시 반영
             */
            bloodUnitRepository
                    .flush();


            int savedCount =
                    end;


            /*
             * 실제 진행률
             */
            int actualPercent =
                    (int) Math.floor(
                            savedCount
                                    * 100.0
                                    / total
                    );


            /*
             * 5% 단위 변환
             *
             * 4%  -> 0
             * 7%  -> 5
             * 14% -> 10
             * 99% -> 95
             */
            int displayPercent =
                    (actualPercent / PROGRESS_STEP)
                            * PROGRESS_STEP;


            /*
             * 마지막 배치는 반드시 100%
             */
            if (savedCount >= total) {

                displayPercent =
                        100;
            }


            /*
             * 이미 출력한 퍼센트는 중복 출력하지 않는다.
             */
            if (displayPercent
                    > lastDisplayedPercent) {


                /*
                 * 5% 단위 값이 여러 단계 건너뛴 경우에도
                 * 순서대로 출력
                 *
                 * 예:
                 * 5 -> 15로 이동했다면
                 *
                 * 10%
                 * 15%
                 *
                 * 모두 출력
                 */
                for (
                        int percent =
                        lastDisplayedPercent
                                + PROGRESS_STEP;

                        percent <= displayPercent;

                        percent += PROGRESS_STEP
                ) {

                    /*
                     * 현재 진행률에 대응하는 예상 저장 건수
                     */
                    int displayCount;


                    if (percent >= 100) {

                        displayCount =
                                total;

                    } else {

                        displayCount =
                                Math.min(
                                        savedCount,
                                        (int) Math.ceil(
                                                total
                                                        * (percent / 100.0)
                                        )
                                );
                    }


                    if (percent >= 100) {

                        log.info(
                                "[더미 생성] [100%] 저장 완료 {}/{}건",
                                total,
                                total
                        );

                    } else {

                        log.info(
                                "[더미 생성] [{}%] 저장 중... {}/{}건",
                                String.format(
                                        "%3d",
                                        percent
                                ),
                                displayCount,
                                total
                        );
                    }


                    lastDisplayedPercent =
                            percent;
                }
            }
        }


        /*
         * 혹시 100% 로그가 출력되지 않은 경우 보장
         */
        if (lastDisplayedPercent < 100) {

            log.info(
                    "[더미 생성] [100%] 저장 완료 {}/{}건",
                    total,
                    total
            );
        }


        log.info(
                "[더미 생성] DB 저장 완료 - 총 {}건",
                total
        );

        log.info(
                "------------------------------------------------------------"
        );
    }


    /**
     * 헌혈자 ID 생성
     *
     * 예:
     * DNR-2026-00000001
     */
    private String createDonorId(
            LocalDate generationDate,
            long sequence
    ) {

        return "DNR-"
                + generationDate.getYear()
                + "-"
                + String.format(
                "%08d",
                sequence
        );
    }


    /**
     * 혈액팩 unitId 생성
     *
     * 예:
     * DUMMY-BLD-20260825-00000001
     */
    private String createUnitId(
            LocalDate generationDate,
            long sequence
    ) {

        return createTodayDummyPrefix(
                generationDate
        )
                + String.format(
                "%08d",
                sequence
        );
    }


    /**
     * 오늘 더미 데이터 Prefix
     *
     * 예:
     * DUMMY-BLD-20260825-
     */
    private String createTodayDummyPrefix(
            LocalDate date
    ) {

        return DUMMY_UNIT_PREFIX
                + date.format(
                UNIT_DATE_FORMAT
        )
                + "-";
    }


    /**
     * 제제별 유효기간
     */
    private int expirationDays(
            String productType
    ) {

        return switch (productType) {

            case "전혈" ->
                    35;

            case "혈장" ->
                    365;

            case "혈소판단종",
                 "혈소판다종" ->
                    5;

            default ->
                    throw new IllegalArgumentException(
                            "지원하지 않는 혈액 제제: "
                                    + productType
                    );
        };
    }


    /**
     * 최종 생성 수량 요약
     */
    private void logStockSummary(
            Map<String, Map<String, Integer>> stockMap,
            int total
    ) {

        log.info(
                "============================================================"
        );

        log.info(
                "[더미 생성] 혈액 제제별 생성 결과"
        );


        for (Map.Entry<String, Map<String, Integer>> productEntry
                : stockMap.entrySet()) {

            String productType =
                    productEntry.getKey();


            Map<String, Integer> bloodTypes =
                    productEntry.getValue();


            int productTotal =
                    bloodTypes
                            .values()
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum();


            log.info(
                    "[더미 생성] {} - 합계={}, A={}, B={}, O={}, AB={}",
                    productType,
                    productTotal,
                    bloodTypes.getOrDefault(
                            "A",
                            0
                    ),
                    bloodTypes.getOrDefault(
                            "B",
                            0
                    ),
                    bloodTypes.getOrDefault(
                            "O",
                            0
                    ),
                    bloodTypes.getOrDefault(
                            "AB",
                            0
                    )
            );
        }


        log.info(
                "[더미 생성] 최종 총합={}건",
                total
        );

        log.info(
                "============================================================"
        );
    }
}