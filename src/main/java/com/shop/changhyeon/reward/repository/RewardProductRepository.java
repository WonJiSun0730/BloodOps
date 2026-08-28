package com.shop.changhyeon.reward.repository;

import com.shop.changhyeon.reward.entity.RewardProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface RewardProductRepository extends JpaRepository<RewardProduct, Long> {
    List<RewardProduct> findAllByOrderByActiveDescIdDesc();
    List<RewardProduct> findByActiveTrueOrderByPointPriceAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from RewardProduct product where product.id = :productId")
    Optional<RewardProduct> findByIdForUpdate(@Param("productId") Long productId);
}
