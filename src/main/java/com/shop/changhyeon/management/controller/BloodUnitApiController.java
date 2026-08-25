package com.shop.changhyeon.management.controller;

import com.shop.changhyeon.management.dto.BloodUnitListResponseDto;
import com.shop.changhyeon.management.dto.BloodUnitResponseDto;
import com.shop.changhyeon.management.entity.BloodUnit;
import com.shop.changhyeon.management.repository.BloodUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/medical/blood-units")
@RequiredArgsConstructor
public class BloodUnitApiController {

    private static final List<String> AVAILABLE_STATUSES =
            List.of("보유중", "Available");
    private static final List<String> DISPOSED_OR_HELD_STATUSES =
            List.of("폐기", "보류", "Disposed", "Hold");

    private final BloodUnitRepository bloodUnitRepository;

    @GetMapping
    public ResponseEntity<BloodUnitListResponseDto> getBloodUnits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Page<BloodUnit> bloodUnits = bloodUnitRepository.findAll(
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "collectionDate")
                )
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);

        BloodUnitListResponseDto response = new BloodUnitListResponseDto(
                bloodUnits.getContent().stream()
                        .map(BloodUnitResponseDto::from)
                        .toList(),
                bloodUnits.getTotalElements(),
                bloodUnits.getTotalPages(),
                bloodUnits.getNumber(),
                bloodUnits.getSize(),
                bloodUnitRepository.countByStatusIn(AVAILABLE_STATUSES),
                bloodUnitRepository.countByStatusInAndExpirationDateBetween(
                        AVAILABLE_STATUSES, now, sevenDaysLater
                ),
                bloodUnitRepository.countByStatusIn(DISPOSED_OR_HELD_STATUSES)
        );

        return ResponseEntity.ok(response);
    }
}
