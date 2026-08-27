package com.shop.changhyeon.donation.repository;

import com.shop.changhyeon.donation.entity.DonationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {

    List<DonationHistory> findByMember_IdOrderByDonationDateDescIdDesc(Long memberId);

    List<DonationHistory> findAllByOrderByRegTimeDescIdDesc();
}
