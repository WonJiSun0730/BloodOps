package com.shop.changhyeon.reward.entity;

import com.shop.member.entity.Member;
import com.shop.reservation.constant.DonationType;
import com.shop.reservation.entity.Reservation;
import com.shop.changhyeon.reward.constant.PointTransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "point_transaction",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_point_transaction_reservation",
                columnNames = "reservation_id"
        ),
        indexes = @Index(
                name = "idx_point_transaction_member_created",
                columnList = "member_id, created_at"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", unique = true, updatable = false)
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, updatable = false, length = 30)
    private PointTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "donation_type", updatable = false, length = 30)
    private DonationType donationType;

    @Column(nullable = false, updatable = false)
    private int points;

    @Column(nullable = false, updatable = false, length = 200)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
