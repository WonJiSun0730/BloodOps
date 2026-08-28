package com.shop.favorite.dto;

import com.shop.favorite.entity.FavoriteCenter;

public record FavoriteCenterResponseDto(
        Long id,
        Long centerId,
        String centerName,
        String address
) {

    public static FavoriteCenterResponseDto from(
            FavoriteCenter favorite
    ) {
        return new FavoriteCenterResponseDto(
                favorite.getId(),
                favorite.getCenter().getId(),
                favorite.getCenter().getCenterName(),
                favorite.getCenter().getAddress()
        );
    }
}