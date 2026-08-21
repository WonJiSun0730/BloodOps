package com.shop.changhyeon.management.controller;

import com.shop.changhyeon.management.dto.BloodStockResponseDto;
import com.shop.changhyeon.management.service.BloodStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockApiController {

    private final BloodStockService bloodStockService;

    @GetMapping("/center/{centerId}")
    public ResponseEntity<List<BloodStockResponseDto>> getStocksByCenter(@PathVariable Long centerId) {
        List<BloodStockResponseDto> stocks = bloodStockService.getStocksByCenter(centerId);
        return ResponseEntity.ok(stocks);
    }
}