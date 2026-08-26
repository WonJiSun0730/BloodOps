package com.shop.center.service;

import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CenterGeocodingService {

    private static final String GEOCODING_URL =
            "https://maps.apigw.ntruss.com/map-geocode/v2/geocode"
                    + "?query={query}";

    private final CenterRepository centerRepository;

    @Value("${naver.maps.client-id}")
    private String clientId;

    @Value("${naver.maps.client-secret}")
    private String clientSecret;

    @Transactional
    public int updateMissingCoordinates() {

        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException(
                    "NAVER Maps 인증 정보가 설정되지 않았습니다."
            );
        }

        List<Center> centers =
                centerRepository.findByLatitudeIsNullOrLongitudeIsNull();

        int updatedCount = 0;

        RestClient restClient = RestClient.create();

        for (Center center : centers) {
            try {
                GeocodingResponse response = restClient.get()
                        .uri(
                                GEOCODING_URL,
                                center.getAddress()
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
                        .body(GeocodingResponse.class);

                if (response == null
                        || response.addresses() == null
                        || response.addresses().isEmpty()) {

                    log.warn(
                            "[센터 좌표] 검색 결과 없음: {} / {}",
                            center.getCenterName(),
                            center.getAddress()
                    );

                    continue;
                }

                GeocodingAddress firstAddress =
                        response.addresses().getFirst();

                // NAVER 응답: x=경도, y=위도
                center.setLongitude(
                        Double.parseDouble(firstAddress.x())
                );

                center.setLatitude(
                        Double.parseDouble(firstAddress.y())
                );

                updatedCount++;

                log.info(
                        "[센터 좌표] {}/{} 완료: {}",
                        updatedCount,
                        centers.size(),
                        center.getCenterName()
                );

            } catch (Exception e) {
                log.error(
                        "[센터 좌표] 변환 실패: {} / {}",
                        center.getCenterName(),
                        center.getAddress(),
                        e
                );
            }
        }

        log.info(
                "[센터 좌표] 전체 완료: 대상 {}개, 성공 {}개",
                centers.size(),
                updatedCount
        );

        return updatedCount;
    }

    private record GeocodingResponse(
            List<GeocodingAddress> addresses
    ) {
    }

    private record GeocodingAddress(
            String x,
            String y
    ) {
    }
}