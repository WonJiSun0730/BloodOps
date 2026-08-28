package com.shop.changhyeon.reward.service;

import com.shop.changhyeon.reward.dto.RewardProductRequest;
import com.shop.changhyeon.reward.dto.RewardProductResponse;
import com.shop.changhyeon.reward.entity.RewardProduct;
import com.shop.changhyeon.reward.repository.RewardProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class RewardProductService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

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

    @Transactional(readOnly = true)
    public RewardProductImage getImage(Long productId) {
        RewardProduct product = findProduct(productId);
        if (!product.hasImage()) {
            throw new IllegalArgumentException("등록된 상품 이미지가 없습니다.");
        }
        return new RewardProductImage(product.getImageData(), product.getImageContentType());
    }

    public RewardProductResponse uploadImage(Long productId, MultipartFile image) {
        validateImage(image);
        RewardProduct product = findProduct(productId);
        try {
            product.updateImage(image.getBytes(), image.getContentType());
        } catch (IOException exception) {
            throw new IllegalStateException("상품 이미지를 저장할 수 없습니다.");
        }
        return RewardProductResponse.from(product);
    }

    public void delete(Long productId) {
        RewardProduct product = findProduct(productId);
        rewardProductRepository.delete(product);
    }

    public RewardProductResponse update(Long productId, RewardProductRequest request) {
        RewardProduct product = findProduct(productId);
        product.update(
                request.name().trim(),
                request.description().trim(),
                request.pointPrice(),
                request.stockQuantity(),
                request.active()
        );
        return RewardProductResponse.from(product);
    }

    private RewardProduct findProduct(Long productId) {
        return rewardProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("첨부할 이미지 파일을 선택해주세요.");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("상품 이미지는 5MB 이하만 등록할 수 있습니다.");
        }
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("JPG, PNG, WebP, GIF 이미지만 등록할 수 있습니다.");
        }
    }

    public record RewardProductImage(byte[] data, String contentType) {
    }
}
