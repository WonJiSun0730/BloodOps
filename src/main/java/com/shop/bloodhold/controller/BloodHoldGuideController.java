package com.shop.bloodhold.controller;

import com.shop.bloodhold.dto.BloodHoldGuideResponse;
import com.shop.bloodhold.service.BloodHoldGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blood-hold")
@RequiredArgsConstructor
public class BloodHoldGuideController {

    private final BloodHoldGuideService bloodHoldGuideService;


    @GetMapping("/me")
    public BloodHoldGuideResponse getMyGuide(
            Authentication authentication
    ) {

        return bloodHoldGuideService.getMyGuide(
                authentication.getName()
        );
    }
}