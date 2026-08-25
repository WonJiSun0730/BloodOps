package com.shop.changhyeon.management.controller;

import com.shop.changhyeon.management.dto.*;
import com.shop.changhyeon.management.service.DonorConfirmationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/medical/donor-confirmations") @RequiredArgsConstructor
public class DonorConfirmationApiController {
    private final DonorConfirmationService service;
    @PostMapping("/identity-verifications") public DonorIdentityResponseDto verify(@Valid @RequestBody DonorIdentityRequestDto dto) { return service.verifyIdentity(dto); }
    @GetMapping("/{id}") public DonorConfirmationResponseDto get(@PathVariable Long id) { return DonorConfirmationResponseDto.from(service.getConfirmation(id)); }
    @PutMapping("/{id}") public ResponseEntity<Map<String,String>> update(@PathVariable Long id,@RequestBody DonorConfirmationDto dto) { service.updateConfirmation(id,dto); return ResponseEntity.ok(Map.of("status","success","message","헌혈자 정보 및 서명이 성공적으로 저장되었습니다.")); }
}
