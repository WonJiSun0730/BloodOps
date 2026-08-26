package com.shop.changhyeon.donation.entity;

import com.shop.reservation.constant.DonationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "donation_history",
        indexes = @Index(
                name = "idx_donation_history_donor_date",
                columnList = "donor_name, donation_date"
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
