package com.shop.changhyeon.reward.entity;

import com.shop.changhyeon.reward.constant.VoucherCodeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reward_voucher_code",
        indexes = @Index(
                name = "idx_reward_voucher_product_status",
                columnList = "product_id, status"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RewardVoucherCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "encrypted_code", nullable = false, updatable = false, length = 1000)
    private String encryptedCode;

    @Column(name = "masked_code", nullable = false, updatable = false, length = 100)
    private String maskedCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VoucherCodeStatus status = VoucherCodeStatus.AVAILABLE;

    @Column(name = "purchase_id", unique = true)
    private Long purchaseId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void assignTo(Long purchaseId) {
        if (status != VoucherCodeStatus.AVAILABLE) {
            throw new IllegalStateException("이미 배정된 상품권 코드입니다.");
        }
        this.purchaseId = purchaseId;
        this.status = VoucherCodeStatus.ASSIGNED;
        this.assignedAt = LocalDateTime.now();
    }
}
