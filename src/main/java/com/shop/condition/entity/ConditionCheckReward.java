package com.shop.condition.entity;

import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.changhyeon.reward.entity.PointTransaction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "condition_check_reward",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_condition_check_reward_donation_history",
                        columnNames = "donation_history_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ConditionCheckReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * 어떤 헌혈 기록에 대해 지급된 컨디션 체크 보상인지 나타냅니다.
     *
     * donation_history_id에 UNIQUE 제약을 걸어
     * 한 번의 헌혈당 보상은 최대 한 번만 생성되도록 합니다.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "donation_history_id",
            nullable = false,
            unique = true,
            updatable = false
    )
    private DonationHistory donationHistory;

    /*
     * 실제로 생성된 포인트 거래 기록입니다.
     *
     * 컨디션 체크 보상 PointTransaction은 reservation을 사용하지 않고,
     * 이 엔티티를 통해 어떤 헌혈 기록의 보상인지 별도로 추적합니다.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "point_transaction_id",
            nullable = false,
            unique = true,
            updatable = false
    )
    private PointTransaction pointTransaction;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}