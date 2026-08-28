package com.shop.changhyeon.reward.controller;

import com.shop.changhyeon.reward.dto.RewardProductRequest;
import com.shop.changhyeon.reward.dto.RewardProductResponse;
import com.shop.changhyeon.reward.service.RewardProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @DeleteMapping("/{productId}")
    public void delete(@PathVariable Long productId) {
        rewardProductService.delete(productId);
    }

    @PutMapping("/{productId}/image")
    public RewardProductResponse uploadImage(
            @PathVariable Long productId,
            @RequestPart("image") MultipartFile image
    ) {
        return rewardProductService.uploadImage(productId, image);
    }

    @PutMapping("/{productId}")
    public RewardProductResponse update(
            @PathVariable Long productId,
            @Valid @RequestBody RewardProductRequest request
    ) {
        return rewardProductService.update(productId, request);
    }
}
