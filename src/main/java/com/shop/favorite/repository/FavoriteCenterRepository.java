package com.shop.favorite.repository;

import com.shop.favorite.entity.FavoriteCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteCenterRepository
        extends JpaRepository<FavoriteCenter, Long> {

    List<FavoriteCenter>
    findByMember_IdOrderByIdDesc(
            Long memberId
    );

    Optional<FavoriteCenter>
    findByMember_IdAndCenter_Id(
            Long memberId,
            Long centerId
    );

    boolean existsByMember_IdAndCenter_Id(
            Long memberId,
            Long centerId
    );

    void deleteByCenter_Id(
            Long centerId
    );
}