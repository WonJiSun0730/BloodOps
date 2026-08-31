package com.shop.changhyeon.reward.service;

import com.shop.changhyeon.reward.constant.*;
import com.shop.changhyeon.reward.dto.*;
import com.shop.changhyeon.reward.entity.*;
import com.shop.changhyeon.reward.repository.*;
import com.shop.changhyeon.reward.security.VoucherCodeCipher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RewardVoucherService {
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final RewardProductRepository productRepository;
    private final RewardVoucherCodeRepository voucherRepository;
    private final VoucherCodeCipher cipher;
    private final SecureRandom random = new SecureRandom();

    public VoucherGenerateResponse generate(Long productId, VoucherGenerateRequest request) {
        RewardProduct product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        if (product.getProductType() != RewardProductType.DIGITAL_VOUCHER) {
            throw new IllegalArgumentException("디지털 코드 상품에만 코드를 입고할 수 있습니다.");
        }
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(request.validDays());
        for (int i = 0; i < request.quantity(); i++) {
            String code = generateCode();
            voucherRepository.save(RewardVoucherCode.builder()
                    .productId(productId)
                    .encryptedCode(cipher.encrypt(code))
                    .maskedCode("BLOODOPS-****-****-" + code.substring(code.length() - 4))
                    .status(VoucherCodeStatus.AVAILABLE)
                    .expiresAt(expiresAt)
                    .build());
        }
        product.increaseStock(request.quantity());
        return new VoucherGenerateResponse(productId, request.quantity(),
                voucherRepository.countByProductIdAndStatus(productId, VoucherCodeStatus.AVAILABLE));
    }

    private String generateCode() {
        StringBuilder value = new StringBuilder("BLOODOPS");
        for (int group = 0; group < 3; group++) {
            value.append("-");
            for (int i = 0; i < 4; i++) {
                value.append(CHARS.charAt(random.nextInt(CHARS.length())));
            }
        }
        return value.toString();
    }
}
