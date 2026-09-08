package com.shop.donationtip.controller;

import com.shop.donationtip.constant.DonationTipCategory;
import com.shop.donationtip.dto.DonationTipRequest;
import com.shop.donationtip.dto.DonationTipResponse;
import com.shop.donationtip.service.DonationTipImageService;
import com.shop.donationtip.service.DonationTipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donation-tips")
@RequiredArgsConstructor
public class DonationTipController {

    private final DonationTipService donationTipService;
    private final DonationTipImageService donationTipImageService;


    @GetMapping
    public List<DonationTipResponse> getAll(
            @RequestParam(
                    required = false
            )
            DonationTipCategory category
    ) {
        return donationTipService.getAll(
                category
        );
    }


    @GetMapping("/latest")
    public List<DonationTipResponse> getLatest() {
        return donationTipService.getLatest();
    }


    @GetMapping("/{tipId}")
    public DonationTipResponse getOne(
            @PathVariable Long tipId
    ) {
        return donationTipService.getOne(
                tipId
        );
    }


    @PostMapping
    public DonationTipResponse create(
            @RequestBody DonationTipRequest request,
            Authentication authentication
    ) {
        return donationTipService.create(
                authentication.getName(),
                request
        );
    }


    @PutMapping("/{tipId}")
    public DonationTipResponse update(
            @PathVariable Long tipId,
            @RequestBody DonationTipRequest request
    ) {
        return donationTipService.update(
                tipId,
                request
        );
    }


    @DeleteMapping("/{tipId}")
    public void delete(
            @PathVariable Long tipId
    ) {
        donationTipService.delete(
                tipId
        );
    }


    @PostMapping("/images")
    public Map<String, String> uploadImage(
            @RequestParam("image")
            MultipartFile image
    ) {
        String imagePath =
                donationTipImageService.save(
                        image
                );

        return Map.of(
                "imagePath",
                imagePath
        );
    }
}