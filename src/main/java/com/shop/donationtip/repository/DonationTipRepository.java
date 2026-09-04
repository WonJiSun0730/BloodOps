package com.shop.donationtip.repository;

import com.shop.donationtip.constant.DonationTipCategory;
import com.shop.donationtip.entity.DonationTip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationTipRepository
        extends JpaRepository<DonationTip, Long> {

    List<DonationTip>
    findTop3ByOrderByRegTimeDesc();

    List<DonationTip>
    findByCategoryOrderByRegTimeDesc(
            DonationTipCategory category
    );
}