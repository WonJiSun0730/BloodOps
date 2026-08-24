package com.shop.changhyeon.management.service;

import com.shop.changhyeon.management.dto.BloodStockResponseDto;
import com.shop.changhyeon.management.dto.BloodStockSyncDto;
import com.shop.changhyeon.management.dto.BloodUnitRegisterDto;
import com.shop.changhyeon.management.dto.StockSummaryDto;
import com.shop.changhyeon.management.dto.TodayReservationDto;
import com.shop.changhyeon.management.entity.BloodStock;
import com.shop.changhyeon.management.entity.BloodUnit;
import com.shop.changhyeon.management.repository.BloodStockRepository;
import com.shop.changhyeon.management.repository.BloodUnitRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BloodStockService {

    private final BloodStockRepository bloodStockRepository;
    private final BloodUnitRepository bloodUnitRepository;

    @PersistenceContext
    private final EntityManager em;

    // 0-1. 크롤러 수집 데이터 동기화
    @Transactional
    public void syncStockData(List<BloodStockSyncDto> dtoList) {
        log.info("[재고 관리] 데이터 동기화 요청 - 건수: {}", dtoList == null ? 0 : dtoList.size());

        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }

        int created = 0;
        int updated = 0;

        for (BloodStockSyncDto dto : dtoList) {
            validateSyncDto(dto);

            BloodStock stock = bloodStockRepository
                    .findByCenterIdAndBloodType(dto.getCenterId(), dto.getBloodType())
                    .orElse(null);

            if (stock == null) {
                BloodStock newStock = BloodStock.builder()
                        .centerId(dto.getCenterId())
                        .bloodType(dto.getBloodType())
                        .quantity(dto.getQuantity())
                        .build();
                bloodStockRepository.save(newStock);
                created++;
            } else {
                stock.updateQuantity(dto.getQuantity());
                updated++;
            }
        }

        log.info("[재고 관리] 데이터 동기화 완료 - 신규: {}, 갱신: {}", created, updated);
    }

    // 0-2. 센터별 재고 조회
    public List<BloodStockResponseDto> getStocksByCenter(Long centerId) {
        if (centerId == null || centerId <= 0) {
            throw new IllegalArgumentException("센터 ID는 1 이상의 값이어야 합니다.");
        }

        log.info("[재고 관리] 센터 ID 기반 재고 조회: {}", centerId);

        return bloodStockRepository.findByCenterId(centerId).stream()
                .map(BloodStockResponseDto::from)
                .toList();
    }

    private void validateSyncDto(BloodStockSyncDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("재고 데이터가 null입니다.");
        }
        if (dto.getCenterId() == null || dto.getCenterId() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 centerId입니다: " + dto.getCenterId());
        }
        if (dto.getBloodType() == null) {
            throw new IllegalArgumentException("bloodType은 필수입니다.");
        }
        if (dto.getQuantity() == null || dto.getQuantity() < 0) {
            throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다: " + dto.getQuantity());
        }
    }

    // 1. 금일 헌혈 예약자 목록 조회
    public List<TodayReservationDto> getTodayReservations(Long centerId) {
        LocalDate today = LocalDate.now();

        String jpql = """
            SELECT new com.shop.changhyeon.management.dto.TodayReservationDto(
                r.id,
                r.memberId,
                s.id,
                s.reservationDate,
                s.startTime,
                s.endTime,
                CAST(s.donationType AS string),
                CAST(r.status AS string)
            )
            FROM Reservation r
            JOIN r.slot s
            WHERE s.centerId = :centerId
              AND s.reservationDate = :today
              AND CAST(r.status AS string) = 'RESERVED'
            ORDER BY s.startTime ASC
        """;

        return em.createQuery(jpql, TodayReservationDto.class)
                .setParameter("centerId", centerId)
                .setParameter("today", today)
                .getResultList();
    }

    // 2. 신규 헌혈 혈액 입고 등록
    @Transactional
    public String registerNewBloodUnit(BloodUnitRegisterDto dto) {
        int randomNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
        String unitId = String.format("BLD-%d-%06d", LocalDateTime.now().getYear(), randomNumber);

        int expirationDays = switch (dto.productType()) {
            case "PLASMA", "혈장" -> 365;
            case "PLATELET", "혈소판단종", "혈소판다종" -> 5;
            default -> 35;
        };

        LocalDateTime now = LocalDateTime.now();

        BloodUnit unit = new BloodUnit();
        unit.setDonorId(dto.donorId());
        unit.setBloodType(dto.bloodType());
        unit.setRhType(dto.rhType());
        unit.setGender(dto.gender());
        unit.setBirthDate(LocalDate.parse(dto.birthDate()));
        unit.setUnitId(unitId);
        unit.setProductType(dto.productType());
        unit.setCollectionDate(now);
        unit.setExpirationDate(now.plusDays(expirationDays));
        unit.setStatus("보유중");

        bloodUnitRepository.save(unit);
        return unitId;
    }

    // 3. FEFO 원칙 기반 혈액 예약
    @Transactional
    public List<String> reserveBloodForCrossMatching(String productType, String bloodType, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("예약 수량은 1개 이상이어야 합니다.");
        }

        List<BloodUnit> availableUnits = bloodUnitRepository.findAvailableStockForUpdate(productType, bloodType);

        if (availableUnits.size() < count) {
            throw new IllegalStateException("가용 재고가 부족합니다. (요청: " + count + "개, 가능: " + availableUnits.size() + "개)");
        }

        List<String> reservedUnitIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BloodUnit unit = availableUnits.get(i);
            unit.setStatus("예약됨");
            reservedUnitIds.add(unit.getUnitId());
        }

        return reservedUnitIds;
    }

    // 4. 출고 처리
    @Transactional
    public boolean issueBloodUnit(String unitId) {
        return bloodUnitRepository.findByUnitId(unitId)
                .filter(unit -> "예약됨".equals(unit.getStatus()))
                .map(unit -> {
                    unit.setStatus("사용완료");
                    return true;
                }).orElse(false);
    }

    // 5. 예약 취소
    @Transactional
    public boolean cancelReservation(String unitId) {
        return bloodUnitRepository.findByUnitId(unitId)
                .filter(unit -> "예약됨".equals(unit.getStatus()))
                .map(unit -> {
                    unit.setStatus("보유중");
                    return true;
                }).orElse(false);
    }

    // 6. 재고 현황 요약
    public List<StockSummaryDto> getStockSummaryByStatus(String status) {
        return bloodUnitRepository.findStockSummaryByStatus(status);
    }
}
