package com.shop.changhyeon.reward.controller;

import com.shop.changhyeon.reward.dto.RewardProductRequest;
import com.shop.changhyeon.reward.dto.RewardProductResponse;
import com.shop.changhyeon.reward.service.RewardProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reward-products")
@RequiredArgsConstructor
public class AdminRewardProductController {

    private final RewardProductService rewardProductService;

    @GetMapping
    public List<RewardProductResponse> getProducts() {
        return rewardProductService.getAdminProducts();
    }

    @PostMapping
    public RewardProductResponse create(@Valid @RequestBody RewardProductRequest request) {
        return rewardProductService.create(request);
    }

    @PutMapping("/{productId}")
    public RewardProductResponse update(
            @PathVariable Long productId,
            @Valid @RequestBody RewardProductRequest request
    ) {
        return rewardProductService.update(productId, request);
    }
}
