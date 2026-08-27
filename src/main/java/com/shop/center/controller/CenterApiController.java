package com.shop.center.controller;

import com.shop.center.dto.CenterMapResponseDto;
import com.shop.center.dto.CenterResponseDto;
import com.shop.center.service.CenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping("/page")
    public Page<CenterResponseDto> getCenterPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page
    ) {

        return centerService.getCenterPage(
                keyword,
                page
        );
    }


    @GetMapping("/map")
    public List<CenterMapResponseDto> getCenterMapCenters(
            @RequestParam(required = false) String keyword
    ) {

        return centerService.getCenterMapCenters(
                keyword
        );
    }

    @GetMapping("/{centerId}")
    public CenterResponseDto getCenter(
            @PathVariable Long centerId
    ) {
        return centerService.getCenter(centerId);
    }
}