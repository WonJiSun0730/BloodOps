package com.shop.changhyeon.reward.controller;

import com.shop.changhyeon.reward.dto.MyPointResponse;
import com.shop.changhyeon.reward.dto.RewardProductResponse;
import com.shop.changhyeon.reward.service.PointRewardService;
import com.shop.changhyeon.reward.service.RewardProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardApiController {

    private final PointRewardService pointRewardService;
    private final RewardProductService rewardProductService;

    @GetMapping("/me")
    public MyPointResponse getMyPoints(Authentication authentication) {
        return pointRewardService.getMyPoints(authentication.getName());
    }

    @GetMapping("/products")
    public List<RewardProductResponse> getProducts() {
        return rewardProductService.getAvailableProducts();
    }

    @GetMapping("/products/{productId}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long productId) {
        var image = rewardProductService.getImage(productId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(image.data());
    }
}
