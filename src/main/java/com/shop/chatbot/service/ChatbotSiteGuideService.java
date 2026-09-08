package com.shop.chatbot.service;

import com.shop.chatbot.dto.ChatbotAction;
import com.shop.chatbot.dto.ChatbotResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ChatbotSiteGuideService {

    public ChatbotResponse tryHandle(
            String message
    ) {

        String normalized =
                message.trim()
                        .toLowerCase(Locale.ROOT);


        /*
         * 증명서 진위확인
         *
         * 증명서 발급보다 먼저 검사해야
         * "증명서 진위 확인" 질문이 단순 발급 안내로 잡히지 않습니다.
         */
        if (
                containsAny(
                        normalized,
                        "증명서 진위",
                        "진위 확인",
                        "진위확인",
                        "진위 여부",
                        "진위여부",
                        "증명서 확인",
                        "발급번호 확인",
                        "발급 번호 확인",
                        "증명서 진짜",
                        "증명서가 진짜"
                )
        ) {

            return new ChatbotResponse(
                    """
                    BloodOps에서는 제출용 헌혈확인증명서의 진위여부를 확인할 수 있습니다.

                    증명서 발급 화면의 '진위여부 확인' 탭에서 제출용 증명서에 표시된 발급번호를 입력해주세요.
                    """.trim(),
                    "CERTIFICATE_VERIFY",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "증명서 진위확인",
                                    "/certificate"
                            )
                    ),
                    List.of(
                            "BloodOps 증명서 진위확인 기능"
                    )
            );
        }


        /*
         * 헌혈확인증명서 발급
         */
        if (
                containsAny(
                        normalized,
                        "증명서 발급",
                        "증명서발급",
                        "헌혈확인증명서",
                        "헌혈 확인 증명서",
                        "헌혈 증명서",
                        "헌혈증명서",
                        "증명서 어디",
                        "증명서 뽑",
                        "증명서 출력",
                        "증명서 저장"
                )
        ) {

            return new ChatbotResponse(
                    """
                    BloodOps에서는 헌혈확인증명서를 온라인으로 발급할 수 있습니다.

                    조회용은 내 헌혈기록 확인용이며, 제출용은 발급번호가 표시되어 진위확인이 가능합니다.
                    발급 후 인쇄하거나 PDF로 저장할 수 있습니다.
                    """.trim(),
                    "CERTIFICATE_ISSUE",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "헌혈확인증명서 발급",
                                    "/certificate"
                            )
                    ),
                    List.of(
                            "BloodOps 증명서 발급 기능"
                    )
            );
        }


        /*
         * 혈액사용 보류 확인
         */
        if (
                containsAny(
                        normalized,
                        "보류 확인",
                        "보류확인",
                        "보류 신청 확인",
                        "보류신청 확인",
                        "혈액사용 보류 확인",
                        "혈액 사용 보류 확인",
                        "보류 상태",
                        "보류상태"
                )
        ) {

            return new ChatbotResponse(
                    """
                    혈액사용 보류 관련 상태는 마이페이지의 혈액사용 보류 확인 화면에서 확인할 수 있습니다.

                    로그인 후 해당 메뉴에서 현재 등록된 내용을 확인해주세요.
                    """.trim(),
                    "BLOOD_HOLD_CHECK",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "혈액사용 보류 확인",
                                    "/mypage/blood-hold/check"
                            )
                    ),
                    List.of(
                            "BloodOps 혈액사용 보류 기능"
                    )
            );
        }


        /*
         * 혈액사용 보류 신청/안내
         */
        if (
                containsAny(
                        normalized,
                        "혈액사용 보류",
                        "혈액 사용 보류",
                        "사용 보류",
                        "사용보류",
                        "헌혈한 피 쓰지",
                        "헌혈한 혈액 쓰지",
                        "피 사용 막",
                        "혈액 사용 막",
                        "보류 신청",
                        "보류신청"
                )
        ) {

            return new ChatbotResponse(
                    """
                    헌혈한 혈액의 사용을 보류해야 하는 경우 BloodOps의 혈액사용 보류 안내 화면에서 관련 절차를 확인할 수 있습니다.

                    로그인 후 마이페이지의 혈액사용 보류 메뉴를 이용해주세요.
                    """.trim(),
                    "BLOOD_HOLD_GUIDE",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "혈액사용 보류 안내",
                                    "/mypage/blood-hold"
                            )
                    ),
                    List.of(
                            "BloodOps 혈액사용 보류 기능"
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
                        "전자 문진",
                        "문진 작성",
                        "문진작성",
                        "문진 어디",
                        "문진 하려",
                        "문진하려",
                        "문진 어떻게",
                        "문진어떻게"
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
         * 혈액검사 결과
         */
        if (
                containsAny(
                        normalized,
                        "검사 결과",
                        "검사결과",
                        "혈액 검사",
                        "혈액검사",
                        "헌혈 검사",
                        "헌혈검사",
                        "검사 어디",
                        "검사 확인"
                )
        ) {

            return new ChatbotResponse(
                    """
                    헌혈 후 제공되는 혈액검사 결과는 마이페이지의 검사결과에서 확인할 수 있습니다.

                    로그인 후 등록된 검사결과를 확인해주세요.
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


    /*
     * Gemini 분류 후 사용할 공개 응답 메서드
     */

    public ChatbotResponse createQuestionnaireResponse() {

        return tryHandle(
                "전자문진"
        );
    }


    public ChatbotResponse createTestResultResponse() {

        return tryHandle(
                "검사 결과"
        );
    }


    public ChatbotResponse createCertificateIssueResponse() {

        return tryHandle(
                "증명서 발급"
        );
    }


    public ChatbotResponse createCertificateVerifyResponse() {

        return tryHandle(
                "증명서 진위확인"
        );
    }


    public ChatbotResponse createBloodHoldGuideResponse() {

        return tryHandle(
                "혈액사용 보류"
        );
    }


    public ChatbotResponse createBloodHoldCheckResponse() {

        return tryHandle(
                "혈액사용 보류 확인"
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
}