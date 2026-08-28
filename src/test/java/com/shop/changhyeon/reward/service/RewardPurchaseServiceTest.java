package com.shop.changhyeon.reward.service;

import com.shop.changhyeon.reward.entity.PointTransaction;
import com.shop.changhyeon.reward.entity.RewardProduct;
import com.shop.changhyeon.reward.entity.RewardPurchase;
import com.shop.changhyeon.reward.repository.PointTransactionRepository;
import com.shop.changhyeon.reward.repository.RewardProductRepository;
import com.shop.changhyeon.reward.repository.RewardPurchaseRepository;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardPurchaseServiceTest {

    @Mock RewardProductRepository rewardProductRepository;
    @Mock RewardPurchaseRepository rewardPurchaseRepository;
    @Mock PointTransactionRepository pointTransactionRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks RewardPurchaseService rewardPurchaseService;

    @Test
    void 구매하면_포인트와_재고를_동시에_차감한다() {
        Member member = new Member();
        member.setId(7L);
        RewardProduct product = RewardProduct.builder()
                .id(3L)
                .name("BloodOps 텀블러")
                .description("상품")
                .pointPrice(10_000)
                .stockQuantity(2)
                .active(true)
                .build();

        when(memberRepository.findByEmail("user@bloodops.local")).thenReturn(member);
        when(rewardProductRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(product));
        when(pointTransactionRepository.sumPointsByMemberId(7L)).thenReturn(13_000L);
        when(rewardPurchaseRepository.save(any(RewardPurchase.class)))
                .thenReturn(RewardPurchase.builder().id(11L).build());

        var response = rewardPurchaseService.purchase(3L, "user@bloodops.local");

        assertThat(response.remainingBalance()).isEqualTo(3_000);
        assertThat(response.remainingStock()).isEqualTo(1);
        verify(pointTransactionRepository).save(any(PointTransaction.class));
        verify(rewardPurchaseRepository).save(any(RewardPurchase.class));
    }

    @Test
    void 포인트가_부족하면_구매할_수_없다() {
        Member member = new Member();
        member.setId(7L);
        RewardProduct product = RewardProduct.builder()
                .id(3L)
                .name("BloodOps 텀블러")
                .pointPrice(10_000)
                .stockQuantity(2)
                .active(true)
                .build();

        when(memberRepository.findByEmail("user@bloodops.local")).thenReturn(member);
        when(rewardProductRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(product));
        when(pointTransactionRepository.sumPointsByMemberId(7L)).thenReturn(5_000L);

        assertThatThrownBy(() -> rewardPurchaseService.purchase(3L, "user@bloodops.local"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("포인트가 부족합니다.");

        assertThat(product.getStockQuantity()).isEqualTo(2);
        verify(pointTransactionRepository, never()).save(any());
        verify(rewardPurchaseRepository, never()).save(any());
    }
}
