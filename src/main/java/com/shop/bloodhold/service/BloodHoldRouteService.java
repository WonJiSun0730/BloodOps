package com.shop.bloodhold.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shop.bloodhold.dto.BloodHoldRouteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class BloodHoldRouteService {

    private static final String DIRECTIONS_URL =
            "https://maps.apigw.ntruss.com/map-direction/v1/driving"
                    + "?start={start}"
                    + "&goal={goal}"
                    + "&option=traoptimal";

    @Value("${naver.maps.client-id}")
    private String clientId;

    @Value("${naver.maps.client-secret}")
    private String clientSecret;


    public BloodHoldRouteResponse findOptimalRoute(
            double startLatitude,
            double startLongitude,
            double goalLatitude,
            double goalLongitude
    ) {

        validateCoordinate(
                startLatitude,
                startLongitude,
                "출발지"
        );

        validateCoordinate(
                goalLatitude,
                goalLongitude,
                "목적지"
        );

        if (
                clientId == null
                        || clientId.isBlank()
                        || clientSecret == null
                        || clientSecret.isBlank()
        ) {
            throw new IllegalStateException(
                    "NAVER Maps 인증 정보가 설정되지 않았습니다."
            );
        }

        String start =
                startLongitude
                        + ","
                        + startLatitude;

        String goal =
                goalLongitude
                        + ","
                        + goalLatitude;


        RestClient restClient =
                RestClient.create();


        DirectionsResponse response =
                restClient.get()
                        .uri(
                                DIRECTIONS_URL,
                                start,
                                goal
                        )
                        .header(
                                "x-ncp-apigw-api-key-id",
                                clientId
                        )
                        .header(
                                "x-ncp-apigw-api-key",
                                clientSecret
                        )
                        .header(
                                "Accept",
                                "application/json"
                        )
                        .retrieve()
                        .body(
                                DirectionsResponse.class
                        );


        if (response == null) {
            throw new IllegalStateException(
                    "NAVER 길찾기 응답이 없습니다."
            );
        }


        if (response.code() != 0) {
            throw new IllegalStateException(
                    response.message() != null
                            ? response.message()
                            : "자동차 경로를 조회하지 못했습니다."
            );
        }


        if (
                response.route() == null
                        || response.route().traoptimal() == null
                        || response.route().traoptimal().isEmpty()
        ) {
            throw new IllegalStateException(
                    "조회 가능한 자동차 경로가 없습니다."
            );
        }


        RouteOption route =
                response.route()
                        .traoptimal()
                        .getFirst();


        if (route.summary() == null) {
            throw new IllegalStateException(
                    "경로 요약 정보를 확인할 수 없습니다."
            );
        }


        List<BloodHoldRouteResponse.RoutePoint> path =
                route.path() == null
                        ? List.of()
                        : route.path()
                        .stream()
                        .filter(
                                point ->
                                        point != null
                                                && point.size() >= 2
                        )
                        .map(
                                point ->
                                        new BloodHoldRouteResponse.RoutePoint(
                                                point.get(0),
                                                point.get(1)
                                        )
                        )
                        .toList();


        if (path.isEmpty()) {
            throw new IllegalStateException(
                    "지도에 표시할 경로 좌표가 없습니다."
            );
        }


        return new BloodHoldRouteResponse(
                route.summary().distance(),
                route.summary().duration(),
                route.summary().tollFare(),
                route.summary().taxiFare(),
                route.summary().fuelPrice(),
                path
        );
    }


    private void validateCoordinate(
            double latitude,
            double longitude,
            String label
    ) {

        if (
                !Double.isFinite(latitude)
                        || !Double.isFinite(longitude)
                        || latitude < -90
                        || latitude > 90
                        || longitude < -180
                        || longitude > 180
        ) {
            throw new IllegalArgumentException(
                    label
                            + " 좌표가 올바르지 않습니다."
            );
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DirectionsResponse(
            int code,
            String message,
            Route route
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Route(
            List<RouteOption> traoptimal
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RouteOption(
            RouteSummary summary,
            List<List<Double>> path
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RouteSummary(
            int distance,
            long duration,
            int tollFare,
            int taxiFare,
            int fuelPrice
    ) {
    }
}