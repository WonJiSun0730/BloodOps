package com.shop.changhyeon.crawler.service;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyBloodDummyService {

    private static final String DUMMY_SOURCE = "DUMMY";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final BloodInfoService bloodInfoService;
    private final BloodUnitRepository bloodUnitRepository;

    @Value("${crawler.dummy-generation.enabled:true}")
    private boolean enabled;

    /**
     * 애플리케이션 시작 시 오늘 생성 여부를 확인한다.
     * 이미 오늘 생성했다면 대량 INSERT를 다시 수행하지 않는다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void generateOnStartup() {
        generateTodayIfNeeded();
    }

    /** 매일 00:10 KST에 최신 공식 보유량으로 갱신한다. */
    @Scheduled(cron = "${crawler.dummy-generation.cron:0 10 0 * * *}", zone = "Asia/Seoul")
    public void generateDaily() {
        generateTodayIfNeeded();
    }

    @Transactional
    public void generateTodayIfNeeded() {
        if (!enabled) {
            log.info("[더미 생성] 비활성화 상태입니다.");
            return;
        }

        LocalDate today = LocalDate.now(KST);

        if (bloodUnitRepository.existsByDataSourceAndDummyGenerationDate(DUMMY_SOURCE, today)) {
            log.info("[더미 생성] 오늘({}) 이미 생성되어 생략합니다.", today);
            return;
        }

        log.info("[더미 생성] 오늘({}) 공식 혈액보유현황 기반 생성을 시작합니다.", today);

        List<RawStockData> officialStocks = bloodInfoService.fetchOfficialProductStockData();
        Map<String, Map<String, Integer>> stockMap = toStockMap(officialStocks);
        addPlasmaStock(stockMap);

        // 이전 DUMMY 데이터만 교체한다. 실제 등록 혈액은 삭제하지 않는다.
        int deleted = bloodUnitRepository.deleteByDataSource(DUMMY_SOURCE);
        log.info("[더미 생성] 기존 DUMMY 데이터 삭제: {}건", deleted);

        List<BloodUnit> units = createUnits(stockMap, today);
        bloodUnitRepository.saveAll(units);

        log.info("[더미 생성] 완료: {}건", units.size());
    }

    private Map<String, Map<String, Integer>> toStockMap(List<RawStockData> stocks) {
        Map<String, Map<String, Integer>> result = new java.util.LinkedHashMap<>();

        for (RawStockData stock : stocks) {
            result.computeIfAbsent(stock.getProductType(), key -> new java.util.LinkedHashMap<>())
                    .put(stock.getBloodType(), stock.getQuantity());
        }

        return result;
    }

    private void addPlasmaStock(Map<String, Map<String, Integer>> stockMap) {
        Map<String, Integer> wholeBlood = stockMap.get("전혈");
        if (wholeBlood == null || wholeBlood.size() != 4) {
            throw new IllegalStateException("전혈 공식 수치가 없어 혈장 더미 데이터를 생성할 수 없습니다.");
        }

        Map<String, Integer> plasma = new java.util.LinkedHashMap<>();
        wholeBlood.forEach((bloodType, quantity) ->
                plasma.put(bloodType, Math.round(quantity * 1.2f))
        );

        stockMap.put("혈장", plasma);
    }

    private List<BloodUnit> createUnits(
            Map<String, Map<String, Integer>> stockMap,
            LocalDate generationDate
    ) {
        List<BloodUnit> result = new ArrayList<>();
        AtomicLong sequence = new AtomicLong(1);

        List<String> productOrder = List.of(
                "전혈",
                "혈장",
                "혈소판단종",
                "혈소판다종"
        );

        for (String productType : productOrder) {
            Map<String, Integer> productStock = stockMap.get(productType);
            if (productStock == null) {
                throw new IllegalStateException("공식 제제 수치가 누락되었습니다: " + productType);
            }

            int expirationDays = expirationDays(productType);

            for (Map.Entry<String, Integer> entry : productStock.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) {
                    result.add(createUnit(
                            productType,
                            entry.getKey(),
                            generationDate,
                            expirationDays,
                            sequence.getAndIncrement()
                    ));
                }
            }
        }

        return result;
    }

    private BloodUnit createUnit(
            String productType,
            String bloodType,
            LocalDate generationDate,
            int expirationDays,
            long sequence
    ) {
        LocalDateTime now = LocalDateTime.now(KST);
        LocalDateTime collectionDate = now.minusHours(sequence % Math.max(1, expirationDays * 24L));

        BloodUnit unit = new BloodUnit();
        unit.setDonorId("DNR-" + generationDate.getYear() + "-" + String.format("%08d", sequence));
        unit.setBloodType(bloodType);
        unit.setRhType(sequence % 10 == 0 ? "Rh-" : "Rh+");
        unit.setGender(sequence % 2 == 0 ? "남" : "여");
        unit.setBirthDate(generationDate.minusYears(20 + sequence % 46).minusDays(sequence % 365));
        unit.setUnitId("BLD-" + generationDate.getYear() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        unit.setProductType(productType);
        unit.setCollectionDate(collectionDate);
        unit.setExpirationDate(collectionDate.plusDays(expirationDays));
        unit.setStatus("보유중");
        unit.setDataSource(DUMMY_SOURCE);
        unit.setDummyGenerationDate(generationDate);
        return unit;
    }

    private int expirationDays(String productType) {
        return switch (productType) {
            case "전혈" -> 35;
            case "혈장" -> 365;
            case "혈소판단종", "혈소판다종" -> 5;
            default -> throw new IllegalArgumentException("지원하지 않는 혈액 제제: " + productType);
        };
    }
}
