package com.shop.changhyeon.reward.repository;

import com.shop.changhyeon.reward.entity.RewardProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardProductRepository extends JpaRepository<RewardProduct, Long> {
    List<RewardProduct> findAllByOrderByActiveDescIdDesc();
    List<RewardProduct> findByActiveTrueOrderByPointPriceAsc();
}
