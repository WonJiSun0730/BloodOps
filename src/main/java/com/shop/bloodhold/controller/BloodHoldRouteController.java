package com.shop.bloodhold.controller;

import com.shop.bloodhold.dto.BloodHoldRouteResponse;
import com.shop.bloodhold.service.BloodHoldRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blood-hold")
@RequiredArgsConstructor
public class BloodHoldRouteController {

    private final BloodHoldRouteService
            bloodHoldRouteService;


    @PostMapping("/route")
    public ResponseEntity<BloodHoldRouteResponse> getRoute(
            @RequestBody BloodHoldRouteRequest request
    ) {

        BloodHoldRouteResponse response =
                bloodHoldRouteService
                        .findOptimalRoute(
                                request.startLatitude(),
                                request.startLongitude(),
                                request.goalLatitude(),
                                request.goalLongitude()
                        );


        return ResponseEntity.ok(
                response
        );
    }


    public record BloodHoldRouteRequest(
            double startLatitude,
            double startLongitude,
            double goalLatitude,
            double goalLongitude
    ) {
    }
}