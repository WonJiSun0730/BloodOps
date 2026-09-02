package com.shop.bloodhold.init;

import com.shop.bloodhold.constant.BloodHoldContactType;
import com.shop.bloodhold.entity.BloodHoldContact;
import com.shop.bloodhold.repository.BloodHoldContactRepository;
import com.shop.bloodhold.service.BloodHoldContactGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class BloodHoldContactInitializer
        implements CommandLineRunner {

    private final BloodHoldContactRepository bloodHoldContactRepository;
    private final BloodHoldContactGeocodingService bloodHoldContactGeocodingService;


    @Override
    public void run(
            String... args
    ) throws Exception {

        ClassPathResource resource =
                new ClassPathResource(
                        "data/blood-hold-contacts.csv"
                );


        if (!resource.exists()) {
            System.out.println(
                    "[혈액사용 보류 기관] CSV 파일이 없습니다."
            );

            return;
        }


        int createdCount = 0;
        int skippedCount = 0;


        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        resource.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            boolean firstLine = true;


            while (
                    (line = reader.readLine())
                            != null
            ) {

                /*
                 * 헤더 건너뛰기
                 */
                if (firstLine) {
                    firstLine = false;

                    continue;
                }


                if (line.isBlank()) {
                    continue;
                }


                /*
                 * 현재 CSV 데이터에는 주소 등에 쉼표가 없으므로
                 * 기본 split 방식으로 충분합니다.
                 *
                 * 빈 parentName / postalCode / fax도 보존하기 위해
                 * limit을 -1로 지정합니다.
                 */
                String[] columns =
                        line.split(
                                ",",
                                -1
                        );


                if (columns.length != 7) {

                    System.out.println(
                            "[혈액사용 보류 기관] 잘못된 CSV 행: "
                                    + line
                    );

                    continue;
                }


                BloodHoldContactType type =
                        BloodHoldContactType.valueOf(
                                columns[0].trim()
                        );


                String name =
                        columns[1].trim();


                if (
                        bloodHoldContactRepository
                                .existsByName(name)
                ) {

                    skippedCount++;

                    continue;
                }


                String parentName =
                        normalizeNullable(
                                columns[2]
                        );


                String postalCode =
                        normalizePostalCode(
                                columns[3]
                        );


                String address =
                        columns[4].trim();


                String phone =
                        columns[5].trim();


                String fax =
                        normalizeNullable(
                                columns[6]
                        );


                BloodHoldContact contact =
                        BloodHoldContact.builder()
                                .type(type)
                                .name(name)
                                .parentName(parentName)
                                .postalCode(postalCode)
                                .address(address)
                                .phone(phone)
                                .fax(fax)
                                .active(true)
                                .build();


                bloodHoldContactRepository.save(
                        contact
                );

                createdCount++;
            }
        }


        System.out.println(
                "[혈액사용 보류 기관] 적재 완료: "
                        + createdCount
                        + "건 생성, "
                        + skippedCount
                        + "건 건너뜀"
        );

        try {

            int geocodedCount =
                    bloodHoldContactGeocodingService
                            .updateMissingCoordinates();

            System.out.println(
                    "[혈액사용 보류 기관] 좌표 변환 완료: "
                            + geocodedCount
                            + "건"
            );

        } catch (Exception e) {

            System.err.println(
                    "[혈액사용 보류 기관] 좌표 변환 실패: "
                            + e.getMessage()
            );
        }
    }


    private String normalizeNullable(
            String value
    ) {

        if (value == null) {
            return null;
        }


        String trimmed =
                value.trim();


        return trimmed.isEmpty()
                ? null
                : trimmed;
    }


    private String normalizePostalCode(
            String rawPostalCode
    ) {

        String value =
                normalizeNullable(
                        rawPostalCode
                );


        if (value == null) {
            return null;
        }


        /*
         * Google Spreadsheet가
         * 07559 → 7559처럼 앞자리 0을 제거한 경우 복원합니다.
         */
        if (
                value.matches("\\d{1,5}")
        ) {

            return String.format(
                    "%05d",
                    Integer.parseInt(value)
            );
        }


        return value;
    }
}