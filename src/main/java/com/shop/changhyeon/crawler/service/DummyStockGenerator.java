package com.shop.changhyeon.crawler.service;

import com.shop.changhyeon.crawler.model.RawStockData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DummyStockGenerator {

    private final Random random = new Random();
    private final String[] bloodTypes = {"A_POSITIVE", "A_NEGATIVE", "B_POSITIVE", "B_NEGATIVE", "O_POSITIVE", "O_NEGATIVE", "AB_POSITIVE", "AB_NEGATIVE"};

    public List<RawStockData> generateDummyData() {
        List<RawStockData> dummyList = new ArrayList<>();

        for (long centerId = 1; centerId <= 3; centerId++) {
            for (String type : bloodTypes) {
                dummyList.add(RawStockData.builder()
                        .centerId(centerId)
                        .bloodType(type)
                        .quantity(random.nextInt(30) + 1)
                        .build());
            }
        }
        return dummyList;
    }
}