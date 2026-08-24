package com.shop.changhyeon.management.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donor_confirmation")
@Getter
@Setter
@NoArgsConstructor
public class DonorConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long confirmationId;

    private String name;
    private String birthDate;
    private Double height;
    private Double weight;
    private String bloodType;
    private String phone;
    private String email;
    private String address;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String signatureData;

    private LocalDateTime updatedAt;

    public void updateDonorConfirmation(String name, String birthDate, Double height, Double weight,
                                        String bloodType, String phone, String email, String address, String signatureData) {
        this.name = name;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.bloodType = bloodType;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.signatureData = signatureData;
        this.updatedAt = LocalDateTime.now();
    }
}
