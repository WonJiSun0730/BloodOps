package com.shop.changhyeon.reward.controller;

import com.shop.changhyeon.reward.dto.MyPointResponse;
import com.shop.changhyeon.reward.dto.RewardProductResponse;
import com.shop.changhyeon.reward.service.PointRewardService;
import com.shop.changhyeon.reward.service.RewardProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}
