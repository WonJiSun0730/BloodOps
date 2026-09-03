package com.shop.condition.repository;

import com.shop.condition.entity.ConditionCheckReward;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionCheckRewardRepository
        extends JpaRepository<ConditionCheckReward, Long> {

    boolean existsByDonationHistory_Id(Long donationHistoryId);
}