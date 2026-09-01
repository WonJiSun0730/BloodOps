package com.shop.favorite.controller;

import com.shop.favorite.dto.FavoriteCenterResponseDto;
import com.shop.favorite.service.FavoriteCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite-centers")
@RequiredArgsConstructor
public class FavoriteCenterController {

    private final FavoriteCenterService favoriteCenterService;


    @GetMapping
    public List<FavoriteCenterResponseDto> getMyFavorites(
            Authentication authentication
    ) {
        return favoriteCenterService.getMyFavorites(
                authentication.getName()
        );
    }


    @PostMapping("/{centerId}")
    public FavoriteCenterResponseDto addFavorite(
            @PathVariable Long centerId,
            Authentication authentication
    ) {
        return favoriteCenterService.addFavorite(
                authentication.getName(),
                centerId
        );
    }


    @DeleteMapping("/{centerId}")
    public void removeFavorite(
            @PathVariable Long centerId,
            Authentication authentication
    ) {
        favoriteCenterService.removeFavorite(
                authentication.getName(),
                centerId
        );
    }
}