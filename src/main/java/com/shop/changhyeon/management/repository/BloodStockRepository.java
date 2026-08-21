package com.shop.changhyeon.management.repository;

import com.shop.changhyeon.management.constant.BloodType;
import com.shop.changhyeon.management.entity.BloodStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BloodStockRepository extends JpaRepository<BloodStock, Long> {
    Optional<BloodStock> findByCenterIdAndBloodType(Long centerId, BloodType bloodType);
    List<BloodStock> findByCenterId(Long centerId);
}