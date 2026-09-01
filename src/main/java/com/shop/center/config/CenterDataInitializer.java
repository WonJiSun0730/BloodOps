package com.shop.center.config;

import com.shop.center.constant.CenterSource;
import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.center.service.CenterCsvImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CenterDataInitializer implements CommandLineRunner {

    private final CenterRepository centerRepository;
    private final CenterCsvImportService centerCsvImportService;


    @Override
    public void run(String... args) {

        /*
         * 센터 데이터가 하나도 없는 최초 실행일 때만
         * 공공데이터 CSV를 적재합니다.
         */
        if (centerRepository.count() == 0) {

            int savedCount =
                    centerCsvImportService.importCenters();

            System.out.println(
                    "[Center] CSV 초기 적재 완료: "
                            + savedCount
                            + "개"
            );

        } else {

            System.out.println(
                    "[Center] 기존 센터 데이터가 존재하여 "
                            + "CSV 적재를 건너뜁니다."
            );
        }


        /*
         * CSV에는 운영시간/헌혈종류 등의 상세정보가 없으므로
         * PUBLIC_DATA 센터 중 값이 비어 있는 항목에만
         * 시연용 기본값을 보강합니다.
         *
         * 관리자가 이미 수정한 값은 덮어쓰지 않습니다.
         */
        int updatedCount =
                fillMissingDemoData();

        System.out.println(
                "[Center] 기본 운영정보 보강 완료: "
                        + updatedCount
                        + "개"
        );
    }


    private int fillMissingDemoData() {

        List<Center> centers =
                centerRepository.findAll();

        int updatedCount = 0;


        for (Center center : centers) {

            /*
             * 관리자 직접 등록 센터는 건드리지 않습니다.
             */
            if (center.getSource() != CenterSource.PUBLIC_DATA) {
                continue;
            }


            boolean changed = false;


            /*
             * 평일 기본 운영
             */
            if (isBlank(center.getWeekdayHours())) {

                center.setWeekdayHours(
                        "09:00 ~ 18:00"
                );

                changed = true;
            }


            /*
             * 토요일 기본 운영
             */
            if (isBlank(center.getSaturdayHours())) {

                center.setSaturdayHours(
                        "09:00 ~ 17:00"
                );

                changed = true;
            }


            /*
             * 일요일 / 공휴일은 빈 값 자체를
             * '휴무' 의미로 사용하므로 채우지 않습니다.
             */


            /*
             * 시연용 기본 헌혈 가능 종류.
             * 실제 센터의 공식 운영정보를 의미하지 않습니다.
             */
            if (isBlank(center.getDonationTypes())) {

                center.setDonationTypes(
                        "전혈, 혈장, 혈소판, 혈소판혈장"
                );

                changed = true;
            }


            /*
             * 정확한 실제 편의시설 데이터가 없으므로
             * 존재하지 않는 시설을 임의로 만들지 않습니다.
             */
            if (isBlank(center.getAmenities())) {

                center.setAmenities(
                        "센터별 편의시설은 방문 전 확인해주세요."
                );

                changed = true;
            }


            /*
             * 주소 자체는 CSV의 실제 데이터를 사용하고,
             * 존재하지 않는 역/출구 정보를 만들어내지 않습니다.
             */
            if (isBlank(center.getDirections())) {

                center.setDirections(
                        "센터 주소를 기준으로 지도에서 위치를 확인해주세요."
                );

                changed = true;
            }


            /*
             * 주차 여부 역시 원본 데이터가 없기 때문에
             * 임의로 주차 가능으로 표시하지 않습니다.
             */
            if (isBlank(center.getParkingInfo())) {

                center.setParkingInfo(
                        "주차 가능 여부는 센터에 방문 전 문의해주세요."
                );

                changed = true;
            }


            if (changed) {
                updatedCount++;
            }
        }


        /*
         * JPA dirty checking에만 의존하지 않고
         * 초기화 작업임을 명확하게 하기 위해 한 번에 저장합니다.
         */
        centerRepository.saveAll(centers);

        return updatedCount;
    }


    private boolean isBlank(String value) {

        return value == null
                || value.isBlank();
    }
}