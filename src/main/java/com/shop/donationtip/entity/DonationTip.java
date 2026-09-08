package com.shop.donationtip.entity;

import com.shop.common.entity.BaseEntity;
import com.shop.donationtip.constant.DonationTipCategory;
import com.shop.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "donation_tip")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DonationTip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_tip_id")
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private DonationTipCategory category;

    @Column(
            nullable = false,
            length = 120
    )
    private String title;

    @OneToMany(
            mappedBy = "donationTip",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("itemOrder ASC")
    private List<DonationTipItem> items =
            new ArrayList<>();


    public DonationTip(
            Member member,
            DonationTipCategory category,
            String title
    ) {
        this.member = member;
        this.category = category;
        this.title = title;
    }


    public void update(
            DonationTipCategory category,
            String title
    ) {
        this.category = category;
        this.title = title;
    }


    public void addItem(
            DonationTipItem item
    ) {
        items.add(item);

        item.assignDonationTip(
                this
        );
    }


    public void clearItems() {
        items.clear();
    }
}