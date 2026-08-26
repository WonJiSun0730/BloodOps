package com.shop.center.runner;

import com.shop.center.service.CenterGeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "naver.maps.geocoding.enabled",
        havingValue = "true"
)
public class CenterGeocodingRunner implements ApplicationRunner {

    private final CenterGeocodingService centerGeocodingService;

    @Override
    public void run(ApplicationArguments args) {

        log.info("[센터 좌표] 좌표 보완 작업을 시작합니다.");

        int updatedCount =
                centerGeocodingService.updateMissingCoordinates();

        log.info(
                "[센터 좌표] 좌표 보완 작업 종료: {}개 저장",
                updatedCount
        );
    }
}