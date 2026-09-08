package com.shop.donationtip.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "donation_tip_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DonationTipItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_tip_item_id")
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "donation_tip_id",
            nullable = false
    )
    private DonationTip donationTip;

    @Column(
            name = "item_order",
            nullable = false
    )
    private Integer itemOrder;

    @Column(
            length = 120
    )
    private String title;

    @Column(
            nullable = false,
            length = 500
    )
    private String content;

    @Column(
            name = "image_path",
            length = 1000
    )
    private String imagePath;


    public DonationTipItem(
            Integer itemOrder,
            String title,
            String content,
            String imagePath
    ) {
        this.itemOrder = itemOrder;
        this.title = title;
        this.content = content;
        this.imagePath = imagePath;
    }


    void assignDonationTip(
            DonationTip donationTip
    ) {
        this.donationTip =
                donationTip;
    }
}