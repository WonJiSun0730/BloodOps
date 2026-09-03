package com.shop.changhyeon.management.controller;

import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.changhyeon.management.dto.*;
import com.shop.changhyeon.management.entity.BloodUnit;
import com.shop.changhyeon.management.repository.BloodUnitRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/medical/blood-units")
@RequiredArgsConstructor
public class BloodUnitApiController {
    private static final List<String> AVAILABLE = List.of("보유중", "Available");
    private static final List<String> UNAVAILABLE = List.of("폐기", "보류", "Disposed", "Hold");

    private final BloodUnitRepository repository;
    private final CenterRepository centerRepository;

    @GetMapping("/centers")
    public List<BloodUnitCenterDto> centers() {
        return centerRepository.findAll().stream()
                .sorted(Comparator.comparing(Center::getCenterName)
                        .thenComparing(Center::getId))
                .map(BloodUnitCenterDto::from)
                .toList();
    }

    @GetMapping
    public BloodUnitListResponseDto list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long centerId,
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate collectionFrom,
            @RequestParam(required = false) LocalDate collectionTo,
            @RequestParam(required = false) LocalDate expirationFrom,
            @RequestParam(required = false) LocalDate expirationTo
    ) {
        Specification<BloodUnit> spec = buildSpecification(
                keyword,
                centerId,
                bloodType,
                productType,
                status,
                collectionFrom,
                collectionTo,
                expirationFrom,
                expirationTo
        );

        Page<BloodUnit> result = repository.findAll(
                spec,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, "collectionDate")
                )
        );

        LocalDateTime now = LocalDateTime.now();
        Specification<BloodUnit> availableSpec =
                spec.and(statusIn(AVAILABLE));
        Specification<BloodUnit> expiringSpec =
                availableSpec.and(expirationBetween(now, now.plusDays(7)));
        Specification<BloodUnit> unavailableSpec =
                spec.and(statusIn(UNAVAILABLE));

        return new BloodUnitListResponseDto(
                result.map(BloodUnitResponseDto::from).getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                repository.count(availableSpec),
                repository.count(expiringSpec),
                repository.count(unavailableSpec)
        );
    }

    @PostMapping
    @Transactional
    public ResponseEntity<BloodUnitResponseDto> create(
            @Valid @RequestBody BloodUnitUpsertDto dto
    ) {
        validateDates(dto);

        if (repository.findByUnitId(dto.unitId()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 Unit ID입니다.");
        }

        Center center = centerRepository.findById(dto.centerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "선택한 센터를 찾을 수 없습니다."
                ));

        BloodUnit saved = repository.save(
                apply(new BloodUnit(), dto, center)
        );

        return ResponseEntity
                .created(URI.create(
                        "/api/medical/blood-units/" + saved.getId()
                ))
                .body(BloodUnitResponseDto.from(saved));
    }

    @PutMapping("/{id}")
    @Transactional
    public BloodUnitResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody BloodUnitStatusUpdateDto dto
    ) {
        BloodUnit unit = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "혈액 재고를 찾을 수 없습니다."
                ));
        unit.setStatus(dto.status());
        return BloodUnitResponseDto.from(unit);
    }

    private Specification<BloodUnit> buildSpecification(
            String keyword,
            Long centerId,
            String bloodType,
            String productType,
            String status,
            LocalDate collectionFrom,
            LocalDate collectionTo,
            LocalDate expirationFrom,
            LocalDate expirationTo
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!keyword.isBlank()) {
                String like =
                        "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("unitId")), like),
                        cb.like(cb.lower(root.get("donorId")), like)
                ));
            }
            if (centerId != null) {
                predicates.add(cb.equal(
                        root.get("center").get("id"),
                        centerId
                ));
            }
            if (has(bloodType)) {
                predicates.add(cb.equal(
                        root.get("bloodType"),
                        bloodType
                ));
            }
            if (has(productType)) {
                predicates.add(cb.equal(
                        root.get("productType"),
                        productType
                ));
            }
            if (has(status)) {
                predicates.add(cb.equal(
                        root.get("status"),
                        status
                ));
            }
            if (collectionFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("collectionDate"),
                        collectionFrom.atStartOfDay()
                ));
            }
            if (collectionTo != null) {
                predicates.add(cb.lessThan(
                        root.get("collectionDate"),
                        collectionTo.plusDays(1).atStartOfDay()
                ));
            }
            if (expirationFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("expirationDate"),
                        expirationFrom.atStartOfDay()
                ));
            }
            if (expirationTo != null) {
                predicates.add(cb.lessThan(
                        root.get("expirationDate"),
                        expirationTo.plusDays(1).atStartOfDay()
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<BloodUnit> statusIn(
            Collection<String> statuses
    ) {
        return (root, query, cb) ->
                root.get("status").in(statuses);
    }

    private Specification<BloodUnit> expirationBetween(
            LocalDateTime from,
            LocalDateTime to
    ) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(
                        root.get("expirationDate"),
                        from
                ),
                cb.lessThanOrEqualTo(
                        root.get("expirationDate"),
                        to
                )
        );
    }

    private void validateDates(BloodUnitUpsertDto dto) {
        if (!dto.expirationDate().isAfter(dto.collectionDate())) {
            throw new IllegalArgumentException(
                    "유효기간은 채혈일 이후여야 합니다."
            );
        }
    }

    private BloodUnit apply(
            BloodUnit unit,
            BloodUnitUpsertDto dto,
            Center center
    ) {
        unit.setDonorId(dto.donorId());
        unit.setBloodType(dto.bloodType());
        unit.setRhType(dto.rhType());
        unit.setGender(dto.gender());
        unit.setBirthDate(dto.birthDate());
        unit.setUnitId(dto.unitId());
        unit.setProductType(dto.productType());
        unit.setCollectionDate(dto.collectionDate());
        unit.setExpirationDate(dto.expirationDate());
        unit.setStatus(dto.status());
        unit.assignCenter(center);
        return unit;
    }

    private boolean has(String value) {
        return value != null && !value.isBlank();
    }
}
