package com.shop.center.entity;

import com.shop.center.constant.CenterSource;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "center")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Center {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * 공공데이터 기본 정보
     */

    @Column(nullable = false)
    private String bloodCenterName;

    @Column(nullable = false)
    private String centerName;

    private String centerType;

    @Column(nullable = false)
    private String address;

    private Double latitude;

    private Double longitude;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CenterSource source = CenterSource.PUBLIC_DATA;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    private String weekdayHours;

    private String saturdayHours;

    private String sundayHours;

    private String holidayHours;

    private String donationTypes;

    private String amenities;

    @Column(length = 1000)
    private String directions;

    @Column(length = 1000)
    private String parkingInfo;
}