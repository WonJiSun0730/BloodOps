package com.shop.changhyeon.crawler.controller;

import com.shop.changhyeon.crawler.dto.BloodStockStatusResponse;
import com.shop.changhyeon.crawler.service.BloodStockStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/blood-stock-status")
@RequiredArgsConstructor
public class BloodStockStatusController {

    private final BloodStockStatusService bloodStockStatusService;

    @GetMapping
    public BloodStockStatusResponse getBloodStockStatus() {
        return bloodStockStatusService.fetchStatus();
    }
}
