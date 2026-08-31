package com.shop.changhyeon.promotion.repository;

import com.shop.changhyeon.promotion.constant.PromotionCategory;
import com.shop.changhyeon.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    @Query("select distinct p from Promotion p left join fetch p.options where p.active = true and p.startDate <= :today and p.endDate >= :today and (:category is null or p.category = :category) order by p.createdAt desc")
    List<Promotion> findPublished(@Param("today") LocalDate today, @Param("category") PromotionCategory category);
    @Query("select distinct p from Promotion p left join fetch p.options where p.id = :id") Optional<Promotion> findDetail(@Param("id") Long id);
    boolean existsByTitle(String title);
}
