package com.shop.changhyeon.crawler.service;

import com.shop.changhyeon.crawler.dto.BloodStockStatusResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class BloodStockStatusServiceTest {

    private final BloodStockStatusService service = new BloodStockStatusService();

    @Test
    void fixture_parsesDailyUsageAndCalculatesStatus() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/fixtures/blood-stock.html")) {
            assertThat(input).isNotNull();
            Document document = Jsoup.parse(
                    new String(input.readAllBytes(), StandardCharsets.UTF_8)
            );

            BloodStockStatusResponse result = service.parseStatus(document);

            assertThat(result.redBloodCell().total().dailyUsage()).isEqualTo(5052);
            assertThat(result.redBloodCell().total().currentStock()).isEqualTo(22553);
            assertThat(result.redBloodCell().total().value()).isEqualTo(4.5);
            assertThat(result.redBloodCell().bloodTypes().get("A").value()).isEqualTo(2.9);

            assertThat(result.apheresisPlatelet().total().dailyUsage()).isEqualTo(310);
            assertThat(result.apheresisPlatelet().total().currentStock()).isEqualTo(1415);
            assertThat(result.apheresisPlatelet().total().value()).isEqualTo(460.0);
            assertThat(result.crisis().level()).isEqualTo("관심");
        }
    }
}
