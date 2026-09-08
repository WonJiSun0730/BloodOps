package com.shop.bloodhold.entity;

import com.shop.bloodhold.constant.BloodHoldContactType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blood_hold_contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodHoldContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BloodHoldContactType type;


    @Column(nullable = false, unique = true)
    private String name;


    private String parentName;


    @Column(length = 5)
    private String postalCode;


    @Column(nullable = false)
    private String address;


    @Column(nullable = false)
    private String phone;


    private String fax;


    private Double latitude;


    private Double longitude;


    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}