package com.shop.changhyeon.donation.entity;

import com.shop.member.entity.Member;
import com.shop.reservation.constant.DonationType;
import com.shop.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "donation_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_donation_history_reservation",
                columnNames = "reservation_id"
        ),
        indexes = @Index(
                name = "idx_donation_history_member_date",
                columnList = "member_id, donation_date"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DonationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "reg_time", nullable = false, updatable = false)
    private LocalDateTime regTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false, updatable = false)
    private Reservation reservation;

    @Column(name = "donor_name", nullable = false, updatable = false)
    private String donorName;

    @Column(name = "donation_date", nullable = false, updatable = false)
    private LocalDate donationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "donation_type", nullable = false, updatable = false, length = 30)
    private DonationType donationType;

    @Column(name = "blood_center_name", nullable = false, updatable = false)
    private String bloodCenterName;

    @Column(name = "donation_place_name", nullable = false, updatable = false)
    private String donationPlaceName;
}
