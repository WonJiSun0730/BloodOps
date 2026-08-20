package com.shop.center.controller;

import com.shop.center.dto.CenterResponseDto;
import com.shop.center.service.CenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centers")
@RequiredArgsConstructor
public class CenterApiController {

    private final CenterService centerService;


    @GetMapping
    public List<CenterResponseDto> getCenters(
            @RequestParam(required = false) String keyword
    ) {
        return centerService.getCenters(keyword);
    }


    @GetMapping("/{centerId}")
    public CenterResponseDto getCenter(
            @PathVariable Long centerId
    ) {
        return centerService.getCenter(centerId);
    }
}