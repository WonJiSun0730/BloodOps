package com.shop.condition.controller;

import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.condition.dto.ConditionCheckRequestDto;
import com.shop.condition.dto.ConditionCheckResponseDto;
import com.shop.condition.service.ConditionCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/condition-checks")
@RequiredArgsConstructor
public class ConditionCheckController {

    private final ConditionCheckService conditionCheckService;

    @GetMapping("/{donationHistoryId}/access")
    public Map<String, Object> verifyAccess(
            @PathVariable Long donationHistoryId,
            Authentication authentication
    ) {
        DonationHistory history =
                conditionCheckService.verifyAccess(
                        donationHistoryId,
                        authentication.getName()
                );

        var center =
                history.getReservation()
                        .getSlot()
                        .getCenter();

        return Map.of(
                "accessible", true,
                "donationHistoryId", donationHistoryId,
                "centerName", center.getCenterName(),
                "centerPhone", center.getPhone() == null
                        ? ""
                        : center.getPhone()
        );
    }

    @PostMapping("/{donationHistoryId}")
    public ResponseEntity<ConditionCheckResponseDto> createCheck(
            @PathVariable Long donationHistoryId,
            @Valid @RequestBody ConditionCheckRequestDto request,
            Authentication authentication
    ) {
        ConditionCheckResponseDto response =
                conditionCheckService.createCheck(
                        donationHistoryId,
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(response);
    }
}