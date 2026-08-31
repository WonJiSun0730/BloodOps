package com.shop.changhyeon.reward.service;

import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.changhyeon.reward.constant.*;
import com.shop.changhyeon.reward.dto.*;
import com.shop.changhyeon.reward.entity.*;
import com.shop.changhyeon.reward.repository.*;
import com.shop.changhyeon.reward.security.VoucherCodeCipher;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RewardPurchaseService {
    private final RewardProductRepository productRepository;
    private final RewardPurchaseRepository purchaseRepository;
    private final RewardVoucherCodeRepository voucherRepository;
    private final PointTransactionRepository pointRepository;
    private final MemberRepository memberRepository;
    private final CenterRepository centerRepository;
    private final VoucherCodeCipher cipher;
    private final SecureRandom random = new SecureRandom();

    public RewardPurchaseResponse purchase(Long productId, String email, RewardPurchaseRequest request) {
        Member member = member(email);
        RewardProduct product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        if (!product.isActive()) throw new IllegalStateException("현재 교환할 수 없는 상품입니다.");
        if (product.getStockQuantity() <= 0) throw new IllegalStateException("품절된 상품입니다.");
        long balance = pointRepository.sumPointsByMemberId(member.getId());
        if (balance < product.getPointPrice()) {
            throw new IllegalStateException("포인트가 " + String.format("%,d", product.getPointPrice() - balance) + "P 부족합니다. 포인트는 헌혈 완료 시에만 적립됩니다.");
        }

        RewardProductType type = product.getProductType() == null ? RewardProductType.PHYSICAL : product.getProductType();
        Center center = null;
        String pickupCode = null;
        RewardPurchaseStatus status;
        if (type == RewardProductType.PHYSICAL) {
            if (request == null || request.pickupCenterId() == null)
                throw new IllegalArgumentException("수령할 헌혈의 집을 선택해주세요.");
            center = centerRepository.findById(request.pickupCenterId())
                    .filter(Center::isActive)
                    .orElseThrow(() -> new IllegalArgumentException("수령 가능한 센터를 찾을 수 없습니다."));
            pickupCode = String.format("BOPS-%06d", random.nextInt(1_000_000));
            status = RewardPurchaseStatus.READY_FOR_PICKUP;
        } else {
            status = RewardPurchaseStatus.COMPLETED;
        }

        product.decreaseStock();
        pointRepository.save(PointTransaction.builder().member(member)
                .transactionType(PointTransactionType.REWARD_PURCHASE)
                .points(-product.getPointPrice()).description(product.getName() + " 상품 교환").build());

        RewardPurchase purchase = purchaseRepository.save(RewardPurchase.builder()
                .member(member).productId(product.getId()).productName(product.getName())
                .pointsSpent(product.getPointPrice()).productType(type).status(status)
                .pickupCenter(center).pickupCode(pickupCode)
                .pickupDeadline(center == null ? null : LocalDateTime.now().plusDays(7)).build());

        if (type == RewardProductType.DIGITAL_VOUCHER) {
            RewardVoucherCode voucher = voucherRepository
                    .findFirstByProductIdAndStatusAndExpiresAtAfterOrderByIdAsc(productId, VoucherCodeStatus.AVAILABLE, LocalDateTime.now())
                    .orElseThrow(() -> new IllegalStateException("사용 가능한 상품권 코드가 없습니다."));
            voucher.assignTo(purchase.getId());
        }
        return new RewardPurchaseResponse(purchase.getId(), product.getName(), type, status,
                product.getPointPrice(), balance - product.getPointPrice(), product.getStockQuantity(), pickupCode);
    }

    @Transactional(readOnly = true)
    public List<RewardOrderResponse> myOrders(String email) {
        Member member = member(email);
        return purchaseRepository.findByMember_IdOrderByCreatedAtDescIdDesc(member.getId()).stream()
                .map(p -> RewardOrderResponse.from(p, voucherRepository.findByPurchaseId(p.getId()).orElse(null))).toList();
    }

    @Transactional(readOnly = true)
    public VoucherCodeResponse revealVoucher(Long purchaseId, String email) {
        Member member = member(email);
        RewardPurchase purchase = purchaseRepository.findByIdAndMember_Id(purchaseId, member.getId())
                .orElseThrow(() -> new IllegalArgumentException("교환 내역을 찾을 수 없습니다."));
        if (purchase.getProductType() != RewardProductType.DIGITAL_VOUCHER)
            throw new IllegalArgumentException("디지털 코드 상품이 아닙니다.");
        RewardVoucherCode voucher = voucherRepository.findByPurchaseId(purchaseId)
                .orElseThrow(() -> new IllegalStateException("배정된 상품권 코드가 없습니다."));
        return new VoucherCodeResponse(cipher.decrypt(voucher.getEncryptedCode()));
    }

    @Transactional(readOnly = true)
    public List<RewardOrderResponse> adminOrders() {
        return purchaseRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(p -> RewardOrderResponse.from(p, voucherRepository.findByPurchaseId(p.getId()).orElse(null))).toList();
    }

    public RewardOrderResponse completePickup(Long purchaseId) {
        RewardPurchase purchase = purchaseRepository.findByIdForUpdate(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("교환 내역을 찾을 수 없습니다."));
        purchase.completePickup();
        return RewardOrderResponse.from(purchase, null);
    }

    public RewardOrderResponse cancel(Long purchaseId) {
        RewardPurchase purchase = purchaseRepository.findByIdForUpdate(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("교환 내역을 찾을 수 없습니다."));
        purchase.cancel();
        RewardProduct product = productRepository.findByIdForUpdate(purchase.getProductId())
                .orElseThrow(() -> new IllegalStateException("환불할 상품 정보를 찾을 수 없습니다."));
        product.increaseStock(1);
        pointRepository.save(PointTransaction.builder().member(purchase.getMember())
                .transactionType(PointTransactionType.REWARD_REFUND).points(purchase.getPointsSpent())
                .description(purchase.getProductName() + " 교환 취소 환불").build());
        return RewardOrderResponse.from(purchase, null);
    }

    private Member member(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        return member;
    }
}
