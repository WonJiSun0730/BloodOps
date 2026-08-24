package com.shop.changhyeon.crawler.service;

import com.shop.changhyeon.crawler.model.RawStockData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BloodInfoServiceTest {

    private BloodInfoService service;

    @BeforeEach
    void setUp() {
        service = new BloodInfoService(new DummyStockGenerator());
    }

    @Test
    void officialFixture_parsesCurrentBloodStockForFourBloodTypes() throws Exception {
        Document document = loadFixture();

        @SuppressWarnings("unchecked")
        List<RawStockData> result = (List<RawStockData>) ReflectionTestUtils.invokeMethod(
                service,
                "parseOfficialStockTable",
                document
        );

        assertThat(result).hasSize(4);
        assertThat(result).allMatch(item -> item.getCenterId().equals(BloodInfoService.NATIONAL_CENTER_ID));

        Map<String, Integer> stocks = result.stream()
                .collect(Collectors.toMap(RawStockData::getBloodType, RawStockData::getQuantity));

        assertThat(stocks)
                .containsEntry("A", 6625)
                .containsEntry("B", 10258)
                .containsEntry("O", 7998)
                .containsEntry("AB", 2942);
    }

    @Test
    void officialFixture_ignoresDailyUsageAndStatusRows() throws Exception {
        Document document = loadFixture();

        @SuppressWarnings("unchecked")
        List<RawStockData> result = (List<RawStockData>) ReflectionTestUtils.invokeMethod(
                service,
                "parseOfficialStockTable",
                document
        );

        Map<String, Integer> stocks = result.stream()
                .collect(Collectors.toMap(RawStockData::getBloodType, RawStockData::getQuantity));

        assertThat(stocks)
                .doesNotContainValue(1732)
                .doesNotContainValue(1334)
                .doesNotContainValue(1432)
                .doesNotContainValue(554)
                .doesNotContainValue(38)
                .doesNotContainValue(77)
                .doesNotContainValue(56)
                .doesNotContainValue(53);
    }

    private Document loadFixture() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/fixtures/blood-stock.html")) {
            assertThat(input).isNotNull();
            String html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return Jsoup.parse(html);
        }
    }
}
