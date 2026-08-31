package com.shop.changhyeon.reward.controller;

import com.shop.changhyeon.reward.dto.*;
import com.shop.changhyeon.reward.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardApiController {
    private final PointRewardService pointRewardService;
    private final RewardProductService productService;
    private final RewardPurchaseService purchaseService;

    @GetMapping("/me")
    public MyPointResponse getMyPoints(Authentication authentication) {
        return pointRewardService.getMyPoints(authentication.getName());
    }
    @GetMapping("/products")
    public List<RewardProductResponse> getProducts() { return productService.getAvailableProducts(); }

    @PostMapping("/products/{productId}/purchase")
    public RewardPurchaseResponse purchase(@PathVariable Long productId,
            @RequestBody(required = false) RewardPurchaseRequest request, Authentication authentication) {
        return purchaseService.purchase(productId, authentication.getName(), request);
    }

    @GetMapping("/orders/me")
    public List<RewardOrderResponse> orders(Authentication authentication) {
        return purchaseService.myOrders(authentication.getName());
    }

    @GetMapping("/orders/{purchaseId}/voucher-code")
    public VoucherCodeResponse voucher(@PathVariable Long purchaseId, Authentication authentication) {
        return purchaseService.revealVoucher(purchaseId, authentication.getName());
    }

    @GetMapping("/products/{productId}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long productId) {
        var image = productService.getImage(productId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS)).body(image.data());
    }
}
