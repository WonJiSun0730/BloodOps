package com.shop.chatbot.service;

import com.shop.changhyeon.crawler.model.RawStockData;
import com.shop.changhyeon.crawler.service.BloodInfoService;
import com.shop.chatbot.dto.ChatbotAction;
import com.shop.chatbot.dto.ChatbotResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ChatbotBloodStockService {

    private final BloodInfoService bloodInfoService;

    public ChatbotBloodStockService(
            BloodInfoService bloodInfoService
    ) {
        this.bloodInfoService = bloodInfoService;
    }


    public ChatbotResponse tryHandle(
            String message
    ) {

        String normalized =
                message.trim()
                        .toLowerCase(Locale.ROOT);


        /*
         * 혈액 수급 위기 단계 자체를 묻는 경우
         */
        if (
                containsAny(
                        normalized,
                        "혈액 수급 위기",
                        "혈액수급위기",
                        "위기 단계",
                        "위기단계",
                        "혈액 단계",
                        "수급 단계",
                        "수급단계",
                        "관심 단계",
                        "주의 단계",
                        "경계 단계",
                        "심각 단계"
                )
        ) {

            return createCrisisGuideResponse();
        }


        /*
         * 혈액 부족 여부 질문
         *
         * 혈액형별 현재 보유 수량은
         * 조회된 데이터끼리 상대 비교합니다.
         *
         * 국가 혈액 수급 위기단계는
         * 적혈구제제 보유일수 기준이므로
         * 단순 수량 비교와 구분해서 안내합니다.
         */
        if (
                containsAny(
                        normalized,
                        "혈액 부족",
                        "혈액부족",
                        "부족한 혈액",
                        "부족한 혈액형",
                        "혈액 부족해",
                        "혈액 부족한",
                        "재고 부족",
                        "재고부족",
                        "피 부족",
                        "피가 부족",
                        "피 모자",
                        "피가 모자",
                        "혈액 모자",
                        "혈액이 모자"
                )
        ) {

            return createBloodStockResponse(true);
        }


        /*
         * 실제 혈액 보유현황 조회
         */
        if (
                containsAny(
                        normalized,
                        "혈액 보유",
                        "혈액보유",
                        "보유 현황",
                        "보유현황",
                        "혈액 재고",
                        "혈액재고",
                        "재고 현황",
                        "재고현황"
                )
        ) {

            return createBloodStockResponse(false);
        }


        return null;
    }


    private ChatbotResponse createBloodStockResponse(
            boolean includeRelativeComparison
    ) {

        List<RawStockData> stocks =
                bloodInfoService.fetchStockData();


        if (
                stocks == null
                        || stocks.isEmpty()
        ) {

            return new ChatbotResponse(
                    """
                    현재 혈액 보유현황 데이터를 확인할 수 없습니다.

                    잠시 후 다시 확인해주세요.
                    """.trim(),
                    "BLOOD_STOCK_UNAVAILABLE",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "혈액 보유현황 보기",
                                    "/blood-status"
                            )
                    ),
                    List.of(
                            "BloodOps 혈액 보유현황"
                    )
            );
        }


        /*
         * 기존 챗봇 동작을 유지하기 위해
         * 전혈 데이터 기준으로 안내합니다.
         */
        List<RawStockData> wholeBloodStocks =
                stocks.stream()
                        .filter(
                                stock ->
                                        "전혈".equals(
                                                stock.getProductType()
                                        )
                        )
                        .toList();


        if (wholeBloodStocks.isEmpty()) {

            return new ChatbotResponse(
                    """
                    현재 전혈 보유현황 데이터를 확인할 수 없습니다.

                    혈액 보유현황 화면에서 최신 정보를 확인해주세요.
                    """.trim(),
                    "BLOOD_STOCK_EMPTY",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "혈액 보유현황 보기",
                                    "/blood-status"
                            )
                    ),
                    List.of(
                            "BloodOps 혈액 보유현황"
                    )
            );
        }


        StringBuilder answer =
                new StringBuilder();


        answer.append(
                "현재 BloodOps에 반영된 전혈 보유현황입니다.\n\n"
        );


        if (includeRelativeComparison) {

            int minimumQuantity =
                    wholeBloodStocks.stream()
                            .mapToInt(
                                    RawStockData::getQuantity
                            )
                            .min()
                            .orElse(0);


            List<String> relativelyLowBloodTypes =
                    wholeBloodStocks.stream()
                            .filter(
                                    stock ->
                                            stock.getQuantity()
                                                    == minimumQuantity
                            )
                            .map(
                                    stock ->
                                            stock.getBloodType()
                                                    + "형"
                            )
                            .toList();


            if (
                    relativelyLowBloodTypes.size()
                            < wholeBloodStocks.size()
            ) {

                answer.append(
                        String.join(
                                ", ",
                                relativelyLowBloodTypes
                        )
                );

                answer.append(
                        "의 보유량이 다른 혈액형보다 상대적으로 적습니다.\n"
                );

                answer.append(
                        "이는 현재 조회된 혈액형별 수량을 비교한 결과이며, "
                                + "그 자체로 공식 혈액 수급 위기단계를 의미하지는 않습니다.\n\n"
                );

            } else {

                answer.append(
                        "현재 조회된 혈액형별 보유량에는 상대적인 차이가 없습니다.\n\n"
                );
            }
        }


        for (RawStockData stock : wholeBloodStocks) {

            answer.append(
                            stock.getBloodType()
                    )
                    .append("형: ")
                    .append(
                            stock.getQuantity()
                    )
                    .append("\n");
        }


        /*
         * BloodOps 혈액 보유현황 화면과
         * 동일한 혈액 수급 위기 단계 기준입니다.
         */
        answer.append(
                """

                혈액 수급 위기 단계 기준
                · 관심: 적혈구제제 5일분 미만
                · 주의: 적혈구제제 3일분 미만
                · 경계: 적혈구제제 2일분 미만
                · 심각: 적혈구제제 1일분 미만

                적정 혈액 보유량은 5일분 이상입니다.
                """
        );


        answer.append(
                "\n혈액 보유량은 변동될 수 있으므로 최신 현황을 기준으로 확인해주세요."
        );


        return new ChatbotResponse(
                answer.toString().trim(),
                "BLOOD_STOCK_RESULT",
                List.of(
                        new ChatbotAction(
                                "LINK",
                                "혈액 보유현황 자세히 보기",
                                "/blood-status"
                        )
                ),
                List.of(
                        "BloodOps 혈액 보유현황 데이터",
                        "BloodOps 혈액 수급 위기 단계 기준"
                )
        );
    }


    private ChatbotResponse createCrisisGuideResponse() {

        String answer =
                """
                혈액 수급 위기 단계는 적혈구제제 보유일수를 기준으로 안내됩니다.

                · 관심: 5일분 미만
                · 주의: 3일분 미만
                · 경계: 2일분 미만
                · 심각: 1일분 미만

                적정 혈액 보유량은 5일분 이상입니다.

                현재 혈액형별 보유현황과 위기 단계는 혈액 보유현황 화면에서 함께 확인할 수 있습니다.
                """.trim();


        return new ChatbotResponse(
                answer,
                "BLOOD_CRISIS_GUIDE",
                List.of(
                        new ChatbotAction(
                                "LINK",
                                "혈액 보유현황 보기",
                                "/blood-status"
                        )
                ),
                List.of(
                        "BloodOps 혈액 수급 위기 단계 기준"
                )
        );
    }


    private boolean containsAny(
            String text,
            String... keywords
    ) {

        for (String keyword : keywords) {

            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    public ChatbotResponse createShortageResponse() {
        return createBloodStockResponse(true);
    }

    public ChatbotResponse createStatusResponse() {
        return createBloodStockResponse(false);
    }
}