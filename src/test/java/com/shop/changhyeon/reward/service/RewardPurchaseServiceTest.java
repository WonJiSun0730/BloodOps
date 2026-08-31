package com.shop.changhyeon.reward.service;

import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.changhyeon.reward.constant.*;
import com.shop.changhyeon.reward.dto.RewardPurchaseRequest;
import com.shop.changhyeon.reward.entity.*;
import com.shop.changhyeon.reward.repository.*;
import com.shop.changhyeon.reward.security.VoucherCodeCipher;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardPurchaseServiceTest {
 @Mock RewardProductRepository productRepository;
 @Mock RewardPurchaseRepository purchaseRepository;
 @Mock RewardVoucherCodeRepository voucherRepository;
 @Mock PointTransactionRepository pointRepository;
 @Mock MemberRepository memberRepository;
 @Mock CenterRepository centerRepository;
 @Mock VoucherCodeCipher cipher;
 @InjectMocks RewardPurchaseService service;

 @Test void 실물상품을_교환하면_포인트와_재고를_차감하고_수령코드를_발급한다(){
  Member member=new Member();member.setId(7L);
  Center center=Center.builder().id(2L).active(true).centerName("강남센터").bloodCenterName("서울").address("서울").build();
  RewardProduct product=RewardProduct.builder().id(3L).name("텀블러").description("상품").pointPrice(10000).stockQuantity(2).productType(RewardProductType.PHYSICAL).active(true).build();
  when(memberRepository.findByEmail("user@test.com")).thenReturn(member);
  when(productRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(product));
  when(centerRepository.findById(2L)).thenReturn(Optional.of(center));
  when(pointRepository.sumPointsByMemberId(7L)).thenReturn(13000L);
  when(purchaseRepository.save(any())).thenAnswer(i->i.getArgument(0));
  var response=service.purchase(3L,"user@test.com",new RewardPurchaseRequest(2L));
  assertThat(response.remainingBalance()).isEqualTo(3000);
  assertThat(response.remainingStock()).isEqualTo(1);
  assertThat(response.status()).isEqualTo(RewardPurchaseStatus.READY_FOR_PICKUP);
  assertThat(response.pickupCode()).startsWith("BOPS-");
  verify(pointRepository).save(any(PointTransaction.class));
 }

 @Test void 포인트가_부족하면_교환할_수_없다(){
  Member member=new Member();member.setId(7L);
  RewardProduct product=RewardProduct.builder().id(3L).name("텀블러").pointPrice(10000).stockQuantity(2).productType(RewardProductType.PHYSICAL).active(true).build();
  when(memberRepository.findByEmail("user@test.com")).thenReturn(member);
  when(productRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(product));
  when(pointRepository.sumPointsByMemberId(7L)).thenReturn(5000L);
  assertThatThrownBy(()->service.purchase(3L,"user@test.com",new RewardPurchaseRequest(2L)))
   .isInstanceOf(IllegalStateException.class).hasMessageContaining("5,000P 부족");
  assertThat(product.getStockQuantity()).isEqualTo(2);
  verify(pointRepository,never()).save(any());
 }
}
