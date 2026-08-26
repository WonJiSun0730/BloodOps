package com.shop.changhyeon.donation.repository;

import com.shop.changhyeon.donation.entity.DonationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {
}
