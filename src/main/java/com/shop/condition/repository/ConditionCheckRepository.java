package com.shop.condition.repository;

import com.shop.condition.entity.ConditionCheck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionCheckRepository
        extends JpaRepository<ConditionCheck, Long> {
}