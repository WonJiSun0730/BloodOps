package com.shop.center.service;

import com.shop.center.constant.CenterSource;
import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

@Service
@RequiredArgsConstructor
public class CenterCsvImportService {

    private final CenterRepository centerRepository;


    @Transactional
    public int importCenters() {

        int savedCount = 0;

        ClassPathResource resource =
                new ClassPathResource(
                        "data/blood_centers.csv"
                );

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        resource.getInputStream(),
                                        Charset.forName("MS949")
                                )
                        )
        ) {

            CSVFormat format =
                    CSVFormat.DEFAULT.builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .get();


            Iterable<CSVRecord> records =
                    format.parse(reader);


            for (CSVRecord record : records) {

                String bloodCenterName =
                        record.get(0).trim();

                String centerName =
                        record.get(1).trim();

                String centerType =
                        record.get(2).trim();

                String address =
                        record.get(3).trim();

                String phone =
                        record.get(4).trim();


                boolean exists =
                        centerRepository
                                .findByCenterNameAndAddress(
                                        centerName,
                                        address
                                )
                                .isPresent();


                if (exists) {
                    continue;
                }


                Center center =
                        Center.builder()
                                .bloodCenterName(
                                        bloodCenterName
                                )
                                .centerName(
                                        centerName
                                )
                                .centerType(
                                        centerType
                                )
                                .address(
                                        address
                                )
                                .phone(
                                        phone
                                )
                                .source(
                                        CenterSource.PUBLIC_DATA
                                )
                                .active(true)
                                .build();


                centerRepository.save(center);

                savedCount++;
            }

        } catch (Exception e) {

            throw new IllegalStateException(
                    "헌혈센터 CSV 적재에 실패했습니다.",
                    e
            );
        }


        return savedCount;
    }
}