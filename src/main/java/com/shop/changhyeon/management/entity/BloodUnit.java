package com.shop.changhyeon.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_units")
@Getter @Setter
@NoArgsConstructor
public class BloodUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String donorId;
    private String bloodType;
    private String rhType;
    private String gender;
    private LocalDate birthDate;

    @Column(unique = true, nullable = false)
    private String unitId;

    private String productType;
    private LocalDateTime collectionDate;
    private LocalDateTime expirationDate;
    private String status;

    @Column(nullable = false)
    private String dataSource;

    private LocalDate dummyGenerationDate;
}
