package com.shop.changhyeon.reward.service;

import com.shop.changhyeon.reward.dto.RewardProductRequest;
import com.shop.changhyeon.reward.dto.RewardProductResponse;
import com.shop.changhyeon.reward.entity.RewardProduct;
import com.shop.changhyeon.reward.repository.RewardProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RewardProductService {

    private final RewardProductRepository rewardProductRepository;

    @Transactional(readOnly = true)
    public List<RewardProductResponse> getAvailableProducts() {
        return rewardProductRepository.findByActiveTrueOrderByPointPriceAsc()
                .stream().map(RewardProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RewardProductResponse> getAdminProducts() {
        return rewardProductRepository.findAllByOrderByActiveDescIdDesc()
                .stream().map(RewardProductResponse::from).toList();
    }

    public RewardProductResponse create(RewardProductRequest request) {
        RewardProduct product = RewardProduct.builder()
                .name(request.name().trim())
                .description(request.description().trim())
                .pointPrice(request.pointPrice())
                .stockQuantity(request.stockQuantity())
                .active(request.active())
                .build();
        return RewardProductResponse.from(rewardProductRepository.save(product));
    }

    public RewardProductResponse update(Long productId, RewardProductRequest request) {
        RewardProduct product = rewardProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.update(
                request.name().trim(),
                request.description().trim(),
                request.pointPrice(),
                request.stockQuantity(),
                request.active()
        );
        return RewardProductResponse.from(product);
    }
}
