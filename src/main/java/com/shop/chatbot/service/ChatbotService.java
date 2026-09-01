package com.shop.chatbot.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.shop.chatbot.dto.ChatbotAction;
import com.shop.chatbot.dto.ChatbotResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ChatbotService {

    private final String apiKey;
    private final String model;

    public ChatbotService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public ChatbotResponse chat(
            String message
    ) {

        ChatbotResponse siteGuideResponse =
                createSiteGuideResponse(message);

        /*
         * BloodOps 자체 기능에 관한 질문이면
         * Gemini를 호출하지 않고 실제 사이트 구조를 기준으로
         * 즉시 답변합니다.
         */
        if (siteGuideResponse != null) {
            return siteGuideResponse;
        }

        /*
         * BloodOps 자체 기능으로 처리할 수 없는
         * 일반적인 헌혈 질문만 Gemini에게 전달합니다.
         */
        return askGemini(message);
    }


    private ChatbotResponse createSiteGuideResponse(
            String message
    ) {

        String normalized =
                message
                        .trim()
                        .toLowerCase(Locale.ROOT);


        /*
         * 예약 내역 조회/변경/취소는
         * 단순 예약 생성 질문보다 먼저 판별합니다.
         */
        if (
                containsAny(
                        normalized,
                        "예약 내역",
                        "예약내역",
                        "예약 확인",
                        "예약확인",
                        "예약 조회",
                        "예약조회",
                        "예약 변경",
                        "예약변경",
                        "예약 취소",
                        "예약취소",
                        "내 예약"
                )
        ) {
            return new ChatbotResponse(
                    """
                    예약 내역은 BloodOps의 예약 내역 화면에서 확인할 수 있습니다.

                    예약된 헌혈 일정과 센터를 확인하고, 가능한 예약은 변경하거나 취소할 수 있습니다.
                    """.trim(),
                    "RESERVATION_LIST",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "예약 내역 보기",
                                    "/reservations"
                            )
                    ),
                    List.of(
                            "BloodOps 예약 기능"
                    )
            );
        }


        /*
         * 신규 헌혈 예약 안내
         */
        if (
                containsAny(
                        normalized,
                        "헌혈 예약",
                        "예약하고",
                        "예약하려",
                        "예약 방법",
                        "예약방법",
                        "예약 어떻게",
                        "예약 어디"
                )
        ) {
            return new ChatbotResponse(
                    """
                    BloodOps에서는 헌혈센터와 날짜를 선택해 헌혈을 예약할 수 있습니다.

                    헌혈센터를 선택한 뒤 날짜, 헌혈 종류, 예약 가능한 시간을 순서대로 선택하여 예약을 진행해주세요.
                    """.trim(),
                    "RESERVATION_GUIDE",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "헌혈 예약하기",
                                    "/reservations/new"
                            ),
                            new ChatbotAction(
                                    "LINK",
                                    "헌혈의집 찾기",
                                    "/centers"
                            )
                    ),
                    List.of(
                            "BloodOps 예약 기능"
                    )
            );
        }


        /*
         * 헌혈센터 검색
         */
        if (
                containsAny(
                        normalized,
                        "헌혈의집",
                        "헌혈의 집",
                        "헌혈센터",
                        "헌혈 센터",
                        "센터 찾",
                        "센터 검색",
                        "센터검색"
                )
        ) {
            return new ChatbotResponse(
                    """
                    BloodOps의 헌혈의집 찾기에서 센터명이나 지역을 검색할 수 있습니다.

                    검색 결과에서 센터를 선택하면 위치와 센터 정보를 확인할 수 있습니다.
                    """.trim(),
                    "CENTER_SEARCH",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "헌혈의집 찾기",
                                    "/centers"
                            )
                    ),
                    List.of(
                            "BloodOps 헌혈센터 정보"
                    )
            );
        }


        /*
         * 전자문진
         */
        if (
                containsAny(
                        normalized,
                        "전자문진",
                        "문진 작성",
                        "문진작성",
                        "문진 어디"
                )
        ) {
            return new ChatbotResponse(
                    """
                    전자문진은 BloodOps의 전자문진 화면에서 작성할 수 있습니다.

                    헌혈 전 문진 항목을 확인하고 현재 상태에 맞게 응답해주세요.
                    """.trim(),
                    "QUESTIONNAIRE",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "전자문진 작성하기",
                                    "/questionnaire"
                            )
                    ),
                    List.of(
                            "BloodOps 전자문진 기능"
                    )
            );
        }


        /*
         * 개인 헌혈 기록
         */
        if (
                containsAny(
                        normalized,
                        "헌혈 기록",
                        "헌혈기록",
                        "헌혈 내역",
                        "헌혈내역"
                )
        ) {
            return new ChatbotResponse(
                    """
                    완료된 헌혈 기록은 마이페이지의 헌혈기록에서 확인할 수 있습니다.
                    """.trim(),
                    "DONATION_RECORD",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "헌혈기록 보기",
                                    "/mypage/records"
                            )
                    ),
                    List.of(
                            "BloodOps 개인 헌혈기록"
                    )
            );
        }


        /*
         * 혈액검사 결과
         */
        if (
                containsAny(
                        normalized,
                        "검사 결과",
                        "검사결과",
                        "혈액 검사",
                        "혈액검사"
                )
        ) {
            return new ChatbotResponse(
                    """
                    헌혈 후 제공되는 혈액검사 결과는 마이페이지의 검사결과에서 확인할 수 있습니다.
                    """.trim(),
                    "TEST_RESULT",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "검사결과 보기",
                                    "/mypage/results"
                            )
                    ),
                    List.of(
                            "BloodOps 혈액검사 결과"
                    )
            );
        }


        return null;
    }


    private boolean containsAny(
            String message,
            String... keywords
    ) {

        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }

        return false;
    }


    private ChatbotResponse askGemini(
            String message
    ) {

        if (
                apiKey == null
                        || apiKey.isBlank()
        ) {
            throw new IllegalStateException(
                    "Gemini API Key가 설정되지 않았습니다."
            );
        }

        try (
                Client client =
                        Client.builder()
                                .apiKey(apiKey)
                                .build()
        ) {

            Content systemInstruction =
                    Content.builder()
                            .role("system")
                            .parts(
                                    List.of(
                                            Part.builder()
                                                    .text(
                                                            """
                                                            당신은 BloodOps 사이트의 헌혈 안내 AI 챗봇입니다.

                                                            현재 질문은 BloodOps 내부 기능 안내만으로
                                                            처리하기 어려운 일반적인 헌혈 질문입니다.

                                                            주요 역할:
                                                            - 헌혈 종류와 일반적인 헌혈 정보를 안내합니다.
                                                            - 헌혈 전후의 일반적인 주의사항을 설명합니다.

                                                            답변 규칙:
                                                            - 항상 한국어로 답변합니다.
                                                            - 핵심 내용 위주로 짧고 명확하게 설명합니다.
                                                            - 일반적인 안내는 3~6문장 정도로 답변합니다.
                                                            - 사용자의 건강 상태를 진단하지 않습니다.
                                                            - 헌혈 가능 여부를 확정적으로 판정하지 않습니다.
                                                            - 약물, 질환, 치료, 수술 등 의학적 판단이 필요한 경우
                                                              헌혈의집 또는 의료진에게 확인하도록 안내합니다.
                                                            - 확인되지 않은 BloodOps 기능이나 메뉴를 만들어내지 않습니다.
                                                            - 실제 예약이나 개인정보 조회를 수행했다고 말하지 않습니다.
                                                            """
                                                    )
                                                    .build()
                                    )
                            )
                            .build();

            GenerateContentConfig config =
                    GenerateContentConfig.builder()
                            .systemInstruction(
                                    systemInstruction
                            )
                            .temperature(0.4F)
                            .maxOutputTokens(2048)
                            .build();

            GenerateContentResponse response =
                    client.models.generateContent(
                            model,
                            message,
                            config
                    );

            String answer = response.text();

            if (answer == null || answer.isBlank()) {
                throw new IllegalStateException("Gemini 응답 내용이 비어 있습니다.");
            }

            return new ChatbotResponse(
                    answer.trim(),
                    "GENERAL_DONATION",
                    List.of(),
                    List.of("Gemini AI 일반 헌혈 안내")
            );

        } catch (Exception e) { throw new IllegalStateException("AI 안내 서비스 호출 중 오류가 발생했습니다.", e); }
    }
}