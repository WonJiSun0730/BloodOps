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
        ReflectionTestUtils.setField(service, "defaultCenterId", 1L);
    }

    @Test
    void crawlHtmlFixture_parsesAllEightBloodTypesForEachCenter() throws Exception {
        Document document = loadFixture();

        @SuppressWarnings("unchecked")
        List<RawStockData> result = (List<RawStockData>) ReflectionTestUtils.invokeMethod(
                service,
                "parseStockTable",
                document
        );

        assertThat(result).hasSize(16);
        assertThat(result.stream().map(RawStockData::getCenterId).distinct())
                .containsExactlyInAnyOrder(101L, 102L);

        Map<String, Integer> center101 = result.stream()
                .filter(item -> item.getCenterId().equals(101L))
                .collect(Collectors.toMap(RawStockData::getBloodType, RawStockData::getQuantity));

        assertThat(center101)
                .containsEntry("A_POSITIVE", 12)
                .containsEntry("A_NEGATIVE", 3)
                .containsEntry("B_POSITIVE", 18)
                .containsEntry("B_NEGATIVE", 2)
                .containsEntry("O_POSITIVE", 24)
                .containsEntry("O_NEGATIVE", 4)
                .containsEntry("AB_POSITIVE", 7)
                .containsEntry("AB_NEGATIVE", 1);
    }

    @Test
    void parseHtmlFixture_ignoresMalformedAndDuplicateRowsUsingLastValue() throws Exception {
        String html = """
                <table class="stock_table">
                  <thead>
                    <tr><th>센터</th><th>A+</th></tr>
                  </thead>
                  <tbody>
                    <tr data-center-id="101"><td>센터1</td><td>10</td></tr>
                    <tr data-center-id="101"><td>센터1</td><td>15</td></tr>
                    <tr><td>센터기본값</td><td>7</td></tr>
                  </tbody>
                </table>
                """;

        Document document = Jsoup.parse(html);

        @SuppressWarnings("unchecked")
        List<RawStockData> result = (List<RawStockData>) ReflectionTestUtils.invokeMethod(
                service,
                "parseStockTable",
                document
        );

        assertThat(result).hasSize(2);
        assertThat(result).anySatisfy(item -> {
            assertThat(item.getCenterId()).isEqualTo(101L);
            assertThat(item.getBloodType()).isEqualTo("A_POSITIVE");
            assertThat(item.getQuantity()).isEqualTo(15);
        });
        assertThat(result).anySatisfy(item -> {
            assertThat(item.getCenterId()).isEqualTo(1L);
            assertThat(item.getBloodType()).isEqualTo("A_POSITIVE");
            assertThat(item.getQuantity()).isEqualTo(7);
        });
    }

    private Document loadFixture() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/fixtures/blood-stock.html")) {
            assertThat(input).isNotNull();
            String html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return Jsoup.parse(html);
        }
    }
}
