package com.shop.changhyeon.management.controller;

import com.shop.changhyeon.management.dto.BloodStockSyncDto;
import com.shop.changhyeon.management.service.BloodStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stock")
@RequiredArgsConstructor
public class StockAdminController {

    private final BloodStockService bloodStockService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncStock(@RequestBody List<BloodStockSyncDto> dtoList) {
        bloodStockService.syncStockData(dtoList);
        return ResponseEntity.ok("혈액 재고 수집 데이터 동기화 완료");
    }
}