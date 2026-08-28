package com.shop.changhyeon.reward.service;

import com.shop.changhyeon.reward.constant.PointTransactionType;
import com.shop.changhyeon.reward.dto.RewardPurchaseResponse;
import com.shop.changhyeon.reward.entity.PointTransaction;
import com.shop.changhyeon.reward.entity.RewardProduct;
import com.shop.changhyeon.reward.entity.RewardPurchase;
import com.shop.changhyeon.reward.repository.PointTransactionRepository;
import com.shop.changhyeon.reward.repository.RewardProductRepository;
import com.shop.changhyeon.reward.repository.RewardPurchaseRepository;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RewardPurchaseService {

    private final RewardProductRepository rewardProductRepository;
    private final RewardPurchaseRepository rewardPurchaseRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final MemberRepository memberRepository;

    public RewardPurchaseResponse purchase(Long productId, String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        RewardProduct product = rewardProductRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (!product.isActive()) {
            throw new IllegalStateException("현재 구매할 수 없는 상품입니다.");
        }
        if (product.getStockQuantity() <= 0) {
            throw new IllegalStateException("품절된 상품입니다.");
        }

        long balance = pointTransactionRepository.sumPointsByMemberId(member.getId());
        if (balance < product.getPointPrice()) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }

        product.decreaseStock();

        PointTransaction pointTransaction = PointTransaction.builder()
                .member(member)
                .transactionType(PointTransactionType.REWARD_PURCHASE)
                .points(-product.getPointPrice())
                .description(product.getName() + " 상품 구매")
                .build();
        pointTransactionRepository.save(pointTransaction);

        RewardPurchase purchase = rewardPurchaseRepository.save(
                RewardPurchase.builder()
                        .member(member)
                        .productId(product.getId())
                        .productName(product.getName())
                        .pointsSpent(product.getPointPrice())
                        .build()
        );

        return new RewardPurchaseResponse(
                purchase.getId(),
                product.getName(),
                product.getPointPrice(),
                balance - product.getPointPrice(),
                product.getStockQuantity()
        );
    }
}
