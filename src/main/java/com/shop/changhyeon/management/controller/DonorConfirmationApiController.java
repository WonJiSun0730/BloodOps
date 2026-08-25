package com.shop.changhyeon.management.controller;

import com.shop.changhyeon.management.dto.DonorConfirmationDto;
import com.shop.changhyeon.management.dto.DonorConfirmationResponseDto;
import com.shop.changhyeon.management.entity.DonorConfirmation;
import com.shop.changhyeon.management.service.DonorConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/medical/donor-confirmations")
@RequiredArgsConstructor
public class DonorConfirmationApiController {

    private final DonorConfirmationService confirmationService;

    @GetMapping("/{confirmationId}")
    public ResponseEntity<DonorConfirmationResponseDto> getConfirmation(
            @PathVariable Long confirmationId) {
        DonorConfirmation confirmation =
                confirmationService.getConfirmation(confirmationId);

        return ResponseEntity.ok(
                DonorConfirmationResponseDto.from(confirmation)
        );
    }

    @PutMapping("/{confirmationId}")
    public ResponseEntity<Map<String, String>> updateConfirmation(
            @PathVariable Long confirmationId,
            @RequestBody DonorConfirmationDto dto) {
        confirmationService.updateConfirmation(confirmationId, dto);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "헌혈자 정보 및 서명이 성공적으로 저장되었습니다."
        ));
    }
}
