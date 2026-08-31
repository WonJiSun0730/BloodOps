package com.shop.changhyeon.reward.entity;

import com.shop.center.entity.Center;
import com.shop.changhyeon.reward.constant.RewardProductType;
import com.shop.changhyeon.reward.constant.RewardPurchaseStatus;
import com.shop.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reward_purchase",
        indexes = @Index(
                name = "idx_reward_purchase_member_created",
                columnList = "member_id, created_at"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RewardPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, updatable = false, length = 100)
    private String productName;

    @Column(name = "points_spent", nullable = false, updatable = false)
    private int pointsSpent;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", length = 30)
    private RewardProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RewardPurchaseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_center_id")
    private Center pickupCenter;

    @Column(name = "pickup_code", length = 30)
    private String pickupCode;

    @Column(name = "pickup_deadline")
    private LocalDateTime pickupDeadline;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void completePickup() {
        if (status != RewardPurchaseStatus.READY_FOR_PICKUP) {
            throw new IllegalStateException("수령 대기 중인 상품만 완료 처리할 수 있습니다.");
        }
        status = RewardPurchaseStatus.PICKED_UP;
        pickedUpAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status != RewardPurchaseStatus.READY_FOR_PICKUP) {
            throw new IllegalStateException("수령 대기 중인 상품만 취소할 수 있습니다.");
        }
        status = RewardPurchaseStatus.CANCELED;
    }
}
