package com.shop.center.config;

import com.shop.center.repository.CenterRepository;
import com.shop.center.service.CenterCsvImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CenterDataInitializer implements CommandLineRunner {
//                                          Spring이 애플리케이션 구성을 끝낸 다음 실행해주는 인터페이스
    private final CenterRepository centerRepository;
    private final CenterCsvImportService centerCsvImportService;


    @Override
    public void run(String... args) {

        // 센터 데이터가 이미 존재하면 CSV를 다시 적재하지 않습니다.
        if (centerRepository.count() > 0) {
            System.out.println(
                    "[Center] 기존 센터 데이터가 존재하여 CSV 적재를 건너뜁니다."
            );

            return;
        }


        int savedCount =
                centerCsvImportService.importCenters();


        System.out.println(
                "[Center] CSV 초기 적재 완료: "
                        + savedCount
                        + "개"
        );
    }
}