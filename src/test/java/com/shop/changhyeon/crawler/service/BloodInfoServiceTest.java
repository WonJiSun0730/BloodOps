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
    void officialFixture_parsesCurrentBloodStockForAllProductsAndBloodTypes() throws Exception {
        Document document = loadFixture();

        @SuppressWarnings("unchecked")
        List<RawStockData> result = (List<RawStockData>) ReflectionTestUtils.invokeMethod(
                service,
                "parseProductStock",
                document
        );

        assertThat(result).hasSize(12);
        assertThat(result).allMatch(item -> item.getCenterId().equals(BloodInfoService.NATIONAL_CENTER_ID));

        Map<String, Map<String, Integer>> stocks = result.stream()
                .collect(Collectors.groupingBy(
                        RawStockData::getProductType,
                        Collectors.toMap(RawStockData::getBloodType, RawStockData::getQuantity)
                ));

        assertThat(stocks).containsKeys("전혈", "혈소판단종", "혈소판다종");

        assertThat(stocks.get("전혈"))
                .containsEntry("A", 5038)
                .containsEntry("B", 8790)
                .containsEntry("O", 6388)
                .containsEntry("AB", 2337);

        assertThat(stocks.get("혈소판다종"))
                .containsEntry("A", 1863)
                .containsEntry("B", 1505)
                .containsEntry("O", 1701)
                .containsEntry("AB", 543);

        assertThat(stocks.get("혈소판단종"))
                .containsEntry("A", 493)
                .containsEntry("B", 395)
                .containsEntry("O", 355)
                .containsEntry("AB", 172);
    }

    @Test
    void officialFixture_ignoresDailyUsageAndStatusRows() throws Exception {
        Document document = loadFixture();

        @SuppressWarnings("unchecked")
        List<RawStockData> result = (List<RawStockData>) ReflectionTestUtils.invokeMethod(
                service,
                "parseProductStock",
                document
        );

        assertThat(result).hasSize(12);
        assertThat(result)
                .extracting(RawStockData::getQuantity)
                .doesNotContain(
                        5052, 1732, 1334, 1432, 554,
                        5, 4, 6, 7
                );
    }

    private Document loadFixture() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/fixtures/blood-stock.html")) {
            assertThat(input).isNotNull();
            String html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return Jsoup.parse(html);
        }
    }
}
