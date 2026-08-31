package com.shop.changhyeon.reward.service;

import com.shop.changhyeon.reward.constant.*;
import com.shop.changhyeon.reward.dto.*;
import com.shop.changhyeon.reward.entity.RewardProduct;
import com.shop.changhyeon.reward.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RewardProductService {
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg","image/png","image/webp","image/gif");
    private final RewardProductRepository productRepository;
    private final RewardVoucherCodeRepository voucherRepository;

    @Transactional(readOnly = true)
    public List<RewardProductResponse> getAvailableProducts() {
        return productRepository.findByActiveTrueOrderByPointPriceAsc().stream().map(RewardProductResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public List<RewardProductResponse> getAdminProducts() {
        return productRepository.findAllByOrderByActiveDescIdDesc().stream().map(RewardProductResponse::from).toList();
    }

    public RewardProductResponse create(RewardProductRequest request) {
        RewardProductType type = request.resolvedProductType();
        int stock = type == RewardProductType.DIGITAL_VOUCHER ? 0 : request.stockQuantity();
        return RewardProductResponse.from(productRepository.save(RewardProduct.builder()
                .name(request.name().trim()).description(request.description().trim())
                .pointPrice(request.pointPrice()).stockQuantity(stock)
                .productType(type).active(request.active()).build()));
    }

    public RewardProductResponse update(Long id, RewardProductRequest request) {
        RewardProduct product = find(id);
        RewardProductType type = request.resolvedProductType();
        if (product.getProductType() != type && product.getStockQuantity() > 0) {
            throw new IllegalStateException("재고가 있는 상품은 상품 유형을 변경할 수 없습니다.");
        }
        int stock = type == RewardProductType.DIGITAL_VOUCHER ? product.getStockQuantity() : request.stockQuantity();
        product.update(request.name().trim(), request.description().trim(), request.pointPrice(), stock, type, request.active());
        return RewardProductResponse.from(product);
    }

    public void delete(Long id) {
        RewardProduct product = find(id);
        if (product.getProductType() == RewardProductType.DIGITAL_VOUCHER) {
            voucherRepository.deleteByProductIdAndStatus(id, VoucherCodeStatus.AVAILABLE);
        }
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public RewardProductImage getImage(Long id) {
        RewardProduct product = find(id);
        if (!product.hasImage()) throw new IllegalArgumentException("등록된 상품 이미지가 없습니다.");
        return new RewardProductImage(product.getImageData(), product.getImageContentType());
    }

    public RewardProductResponse uploadImage(Long id, MultipartFile image) {
        validateImage(image);
        RewardProduct product = find(id);
        try { product.updateImage(image.getBytes(), image.getContentType()); }
        catch (IOException e) { throw new IllegalStateException("상품 이미지를 저장할 수 없습니다."); }
        return RewardProductResponse.from(product);
    }

    private RewardProduct find(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }
    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) throw new IllegalArgumentException("첨부할 이미지 파일을 선택해주세요.");
        if (image.getSize() > MAX_IMAGE_SIZE) throw new IllegalArgumentException("상품 이미지는 5MB 이하만 등록할 수 있습니다.");
        if (image.getContentType() == null || !ALLOWED_IMAGE_TYPES.contains(image.getContentType()))
            throw new IllegalArgumentException("JPG, PNG, WebP, GIF 이미지만 등록할 수 있습니다.");
    }
    public record RewardProductImage(byte[] data, String contentType) {}
}
