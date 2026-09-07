package com.shop.changhyeon.reward.controller;

import com.shop.changhyeon.reward.dto.*;
import com.shop.changhyeon.reward.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reward-fulfillment")
@RequiredArgsConstructor
public class AdminRewardFulfillmentController {
    private static final int ORDER_PAGE_SIZE = 10;

    private final RewardVoucherService voucherService;
    private final RewardPurchaseService purchaseService;
    private final PointRewardService pointRewardService;

    @PostMapping("/test-points")
    public MyPointResponse addTestPoints(
            @Valid @RequestBody AdminTestPointRequest request,
            Authentication authentication
    ) {
        return pointRewardService.addAdminTestPoints(
                authentication.getName(),
                request.points()
        );
    }

    @PostMapping("/products/{productId}/voucher-codes")
    public VoucherGenerateResponse generate(
            @PathVariable Long productId,
            @Valid @RequestBody VoucherGenerateRequest request
    ) {
        return voucherService.generate(productId, request);
    }

    @GetMapping("/orders")
    public Page<RewardOrderResponse> orders(
            @RequestParam(required = false) Long centerId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return purchaseService.adminOrders(
                centerId,
                PageRequest.of(Math.max(page, 0), ORDER_PAGE_SIZE)
        );
    }

    @PatchMapping("/orders/{purchaseId}/pickup")
    public RewardOrderResponse pickup(@PathVariable Long purchaseId) {
        return purchaseService.completePickup(purchaseId);
    }

    @PatchMapping("/orders/{purchaseId}/cancel")
    public RewardOrderResponse cancel(@PathVariable Long purchaseId) {
        return purchaseService.cancel(purchaseId);
    }
}
