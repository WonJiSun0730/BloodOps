package com.shop.changhyeon.management.entity;

import com.shop.center.entity.Center;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_units")
@Getter
@Setter
@NoArgsConstructor
public class BloodUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donor_id")
    private String donorId;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(name = "rh_type")
    private String rhType;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "unit_id", unique = true, nullable = false)
    private String unitId;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "collection_date")
    private LocalDateTime collectionDate;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    public void assignCenter(Center center) {
        if (center == null || !center.isActive()) {
            throw new IllegalArgumentException("활성화된 센터만 혈액 재고에 배정할 수 있습니다.");
        }
        this.center = center;
    }
}
