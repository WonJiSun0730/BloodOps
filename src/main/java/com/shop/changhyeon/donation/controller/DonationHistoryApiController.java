package com.shop.changhyeon.donation.controller;

import com.shop.changhyeon.donation.dto.DonationCompleteRequestDto;
import com.shop.changhyeon.donation.dto.DonationHistoryResponseDto;
import com.shop.changhyeon.donation.dto.MedicalDonationHistoryResponseDto;
import com.shop.changhyeon.donation.service.DonationHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DonationHistoryApiController {

    private final DonationHistoryService donationHistoryService;

    @PostMapping("/medical/donations/reservations/{reservationId}/complete")
    public DonationHistoryResponseDto completeDonation(
            @PathVariable Long reservationId,
            @RequestBody(required = false) DonationCompleteRequestDto request
    ) {
        return donationHistoryService.completeDonation(
                reservationId,
                request == null ? null : request.donationDate()
        );
    }

    @GetMapping("/donations/me")
    public List<DonationHistoryResponseDto> getMyDonationHistory(
            Authentication authentication
    ) {
        return donationHistoryService.getMyDonationHistory(authentication.getName());
    }

    @GetMapping("/medical/donations")
    public List<MedicalDonationHistoryResponseDto> getAllDonationHistory() {
        return donationHistoryService.getAllDonationHistory();
    }

}
