package com.shop.center.controller;

import com.shop.center.dto.CenterCreateRequestDto;
import com.shop.center.dto.CenterResponseDto;
import com.shop.center.dto.CenterUpdateRequestDto;
import com.shop.center.service.CenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/centers")
@RequiredArgsConstructor
public class CenterAdminController {
    private final CenterService centerService;

    @GetMapping
    public Page<CenterResponseDto> getCenters(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.ASC,
                                "centerName"
                        )
                );

        return centerService.getAdminCenters(
                keyword,
                pageable
        );
    }

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