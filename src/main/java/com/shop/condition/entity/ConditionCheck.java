package com.shop.condition.entity;

import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.condition.constant.OverallCondition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "condition_check",
        indexes = {
                @Index(
                        name = "idx_condition_check_donation_created",
                        columnList = "donation_history_id, created_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ConditionCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * 어떤 헌혈 이후에 실시한 컨디션 체크인지 기록합니다.
     *
     * 한 번의 헌혈에 대해 사용자가 여러 번
     * 컨디션 체크를 실시할 수 있으므로 ManyToOne입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "donation_history_id",
            nullable = false,
            updatable = false
    )
    private DonationHistory donationHistory;

    /*
     * 어지러움
     */
    @Column(nullable = false)
    private boolean dizziness;

    /*
     * 메스꺼움
     */
    @Column(nullable = false)
    private boolean nausea;

    /*
     * 헌혈 부위의 출혈 또는 심한 멍
     */
    @Column(name = "bleeding_or_bruising", nullable = false)
    private boolean bleedingOrBruising;

    /*
     * 헌혈한 팔의 통증
     */
    @Column(name = "arm_pain", nullable = false)
    private boolean armPain;

    /*
     * 실신했거나 실신할 것 같은 느낌
     */
    @Column(name = "fainting", nullable = false)
    private boolean fainting;

    /*
     * 사용자가 직접 입력하는 기타 증상.
     * 없으면 null을 허용합니다.
     */
    @Column(name = "other_symptom", length = 500)
    private String otherSymptom;

    /*
     * 체크 당시 사용자가 선택한 전반적인 상태.
     *
     * GOOD      : 괜찮음
     * CAUTION   : 조금 불편함
     * BAD       : 많이 불편함
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "overall_condition",
            nullable = false,
            length = 20
    )
    private OverallCondition overallCondition;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}