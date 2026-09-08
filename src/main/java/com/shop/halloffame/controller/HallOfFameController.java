package com.shop.halloffame.controller;

import com.shop.halloffame.dto.DonationRankingResponse;
import com.shop.halloffame.dto.MyDonationSummaryResponse;
import com.shop.halloffame.service.HallOfFameService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HallOfFameController {

    private final HallOfFameService hallOfFameService;


    @GetMapping(
            "/api/public/donation-rankings"
    )
    public List<DonationRankingResponse>
    getDonationRankings() {
        return hallOfFameService
                .getAllRankings();
    }


    @GetMapping(
            "/api/donations/me/summary"
    )
    public MyDonationSummaryResponse
    getMyDonationSummary(
            Authentication authentication
    ) {
        return hallOfFameService
                .getMyDonationSummary(
                        authentication.getName()
                );
    }
}