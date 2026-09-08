package com.shop.bloodhold.dto;

import java.util.List;

public record BloodHoldRouteResponse(
        int distanceMeters,
        long durationMillis,
        int tollFare,
        int taxiFare,
        int fuelPrice,
        List<RoutePoint> path
) {

    public record RoutePoint(
            double longitude,
            double latitude
    ) {
    }
}