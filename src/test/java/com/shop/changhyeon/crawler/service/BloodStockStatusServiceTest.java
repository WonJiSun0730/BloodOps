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

            assertThat(result.concentratedPlatelet().total().dailyUsage()).isEqualTo(1250);
            assertThat(result.concentratedPlatelet().total().currentStock()).isEqualTo(5612);
            assertThat(result.concentratedPlatelet().total().value()).isEqualTo(450.0);

            assertThat(result.apheresisPlatelet().total().dailyUsage()).isEqualTo(310);
            assertThat(result.apheresisPlatelet().total().currentStock()).isEqualTo(1415);
            assertThat(result.apheresisPlatelet().total().value()).isEqualTo(460.0);
            assertThat(result.crisis().level()).isEqualTo("관심");
        }
    }

    @Test
    void combinedPlateletTable_accountsForProductNameCell() {
        Document document = Jsoup.parse("""
                <h2>적혈구제제 보유 현황</h2>
                <table>
                  <tr><th>구분</th><th>합계</th><th>A형</th><th>B형</th><th>O형</th><th>AB형</th></tr>
                  <tr><td>1일 소요량</td><td>5052</td><td>1732</td><td>1334</td><td>1432</td><td>554</td></tr>
                  <tr><td>현재 혈액보유량</td><td>20313</td><td>4764</td><td>8133</td><td>5487</td><td>1929</td></tr>
                </table>
                <h2>혈소판제제 보유 현황</h2>
                <table>
                  <tr><th>구분</th><th>합계</th><th>A형</th><th>B형</th><th>O형</th><th>AB형</th></tr>
                  <tr><th>농축혈소판</th><td>1일 소요량</td><td>4572</td><td>1555</td><td>1219</td><td>1277</td><td>521</td></tr>
                  <tr><td>현재 혈액보유량</td><td>6342</td><td>1938</td><td>1778</td><td>2019</td><td>607</td></tr>
                  <tr><th>성분채혈혈소판</th><td>1일 소요량</td><td>673</td><td>228</td><td>182</td><td>164</td><td>99</td></tr>
                  <tr><td>현재 혈액보유량</td><td>1121</td><td>342</td><td>378</td><td>269</td><td>132</td></tr>
                </table>
                """);

        BloodStockStatusResponse result = service.parseStatus(document);

        assertThat(result.concentratedPlatelet().total().dailyUsage()).isEqualTo(4572);
        assertThat(result.concentratedPlatelet().bloodTypes().get("A").dailyUsage()).isEqualTo(1555);
        assertThat(result.apheresisPlatelet().total().dailyUsage()).isEqualTo(673);
        assertThat(result.apheresisPlatelet().total().currentStock()).isEqualTo(1121);
        assertThat(result.apheresisPlatelet().total().value()).isEqualTo(170.0);
        assertThat(result.apheresisPlatelet().bloodTypes().get("B").value()).isEqualTo(210.0);
    }
}
