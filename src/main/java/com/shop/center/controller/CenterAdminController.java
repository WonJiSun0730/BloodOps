package com.shop.center.controller;

import com.shop.center.dto.CenterCreateRequestDto;
import com.shop.center.dto.CenterResponseDto;
import com.shop.center.dto.CenterUpdateRequestDto;
import com.shop.center.service.CenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/centers")
@RequiredArgsConstructor
public class CenterAdminController {
    private final CenterService centerService;

    @PostMapping
    public CenterResponseDto createCenter(
            @RequestBody CenterCreateRequestDto request
    ) {

        return centerService.createCenter(
                request
        );
    }

    @PutMapping("/{centerId}")
    public CenterResponseDto updateCenter(
            @PathVariable Long centerId,
            @RequestBody CenterUpdateRequestDto request
    ) {

        return centerService.updateCenter(
                centerId,
                request
        );
    }
}