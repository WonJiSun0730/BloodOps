package com.shop.changhyeon.management.controller;

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

    @GetMapping
    public BloodUnitListResponseDto list(@RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size, @RequestParam(defaultValue="") String keyword,
            @RequestParam(required=false) String bloodType, @RequestParam(required=false) String productType,
            @RequestParam(required=false) String status, @RequestParam(required=false) LocalDate collectionFrom,
            @RequestParam(required=false) LocalDate collectionTo, @RequestParam(required=false) LocalDate expirationFrom,
            @RequestParam(required=false) LocalDate expirationTo) {
        Specification<BloodUnit> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (!keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("unitId")), like), cb.like(cb.lower(root.get("donorId")), like)));
            }
            if (has(bloodType)) p.add(cb.equal(root.get("bloodType"), bloodType));
            if (has(productType)) p.add(cb.equal(root.get("productType"), productType));
            if (has(status)) p.add(cb.equal(root.get("status"), status));
            if (collectionFrom != null) p.add(cb.greaterThanOrEqualTo(root.get("collectionDate"), collectionFrom.atStartOfDay()));
            if (collectionTo != null) p.add(cb.lessThan(root.get("collectionDate"), collectionTo.plusDays(1).atStartOfDay()));
            if (expirationFrom != null) p.add(cb.greaterThanOrEqualTo(root.get("expirationDate"), expirationFrom.atStartOfDay()));
            if (expirationTo != null) p.add(cb.lessThan(root.get("expirationDate"), expirationTo.plusDays(1).atStartOfDay()));
            return cb.and(p.toArray(Predicate[]::new));
        };
        Page<BloodUnit> result = repository.findAll(spec, PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1),100), Sort.by(Sort.Direction.DESC,"collectionDate")));
        LocalDateTime now = LocalDateTime.now();
        return new BloodUnitListResponseDto(result.map(BloodUnitResponseDto::from).getContent(), result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize(), repository.countByStatusIn(AVAILABLE), repository.countByStatusInAndExpirationDateBetween(AVAILABLE, now, now.plusDays(7)), repository.countByStatusIn(UNAVAILABLE));
    }

    @PostMapping @Transactional
    public ResponseEntity<BloodUnitResponseDto> create(@Valid @RequestBody BloodUnitUpsertDto dto) {
        validateDates(dto);
        if (repository.findByUnitId(dto.unitId()).isPresent()) throw new IllegalArgumentException("이미 등록된 Unit ID입니다.");
        BloodUnit saved = repository.save(apply(new BloodUnit(), dto));
        return ResponseEntity.created(URI.create("/api/medical/blood-units/" + saved.getId())).body(BloodUnitResponseDto.from(saved));
    }

    @PutMapping("/{id}") @Transactional
    public BloodUnitResponseDto update(@PathVariable Long id, @Valid @RequestBody BloodUnitStatusUpdateDto dto) {
        BloodUnit unit = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("혈액 재고를 찾을 수 없습니다."));
        unit.setStatus(dto.status());
        return BloodUnitResponseDto.from(unit);
    }

    private void validateDates(BloodUnitUpsertDto dto) {
        if (!dto.expirationDate().isAfter(dto.collectionDate())) {
            throw new IllegalArgumentException("유효기간은 채혈일 이후여야 합니다.");
        }
    }

    private BloodUnit apply(BloodUnit u, BloodUnitUpsertDto d) { u.setDonorId(d.donorId()); u.setBloodType(d.bloodType()); u.setRhType(d.rhType()); u.setGender(d.gender()); u.setBirthDate(d.birthDate()); u.setUnitId(d.unitId()); u.setProductType(d.productType()); u.setCollectionDate(d.collectionDate()); u.setExpirationDate(d.expirationDate()); u.setStatus(d.status()); return u; }
    private boolean has(String value) { return value != null && !value.isBlank(); }
}