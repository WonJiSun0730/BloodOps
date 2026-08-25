package com.shop.changhyeon.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="donor_confirmation") @Getter @Setter @NoArgsConstructor
public class DonorConfirmation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long confirmationId;
    private String name;
    private String birthDate;
    @Column(name="resident_number_hash", length=64) private String residentNumberHash;
    @Column(name="resident_number_masked", length=14) private String residentNumberMasked;
    private Double height;
    private Double weight;
    private String bloodType;
    private String phone;
    private String email;
    private String address;
    @Lob @Column(columnDefinition="LONGTEXT") private String signatureData;
    private LocalDateTime updatedAt;

    public void updateDonorConfirmation(String name, String birthDate, Double height, Double weight, String bloodType, String phone, String email, String address, String signatureData) {
        this.name=name; this.birthDate=birthDate; this.height=height; this.weight=weight; this.bloodType=bloodType; this.phone=phone; this.email=email; this.address=address; this.signatureData=signatureData; this.updatedAt=LocalDateTime.now();
    }
}
