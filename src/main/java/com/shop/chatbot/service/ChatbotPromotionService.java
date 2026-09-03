package com.shop.chatbot.service;

import com.shop.changhyeon.promotion.constant.PromotionCategory;
import com.shop.changhyeon.promotion.dto.PromotionResponse;
import com.shop.changhyeon.promotion.service.PromotionService;
import com.shop.chatbot.dto.ChatbotAction;
import com.shop.chatbot.dto.ChatbotResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ChatbotPromotionService {

    private final PromotionService promotionService;

    public ChatbotPromotionService(
            PromotionService promotionService
    ) {
        this.promotionService = promotionService;
    }


    /*
     * 사용자의 명확한 프로모션 관련 질문을
     * Gemini 호출 전에 빠르게 처리합니다.
     */
    public ChatbotResponse tryHandle(
            String message
    ) {

        String normalized =
                message.trim()
                        .toLowerCase(Locale.ROOT);


        if (
                !containsAny(
                        normalized,
                        "프로모션",
                        "이벤트",
                        "행사",
                        "경품",
                        "응모",
                        "사은품",
                        "혜택"
                )
        ) {
            return null;
        }


        PromotionResponse matchedPromotion =
                findMatchedPromotion(
                        normalized
                );


        if (matchedPromotion != null) {

            return createPromotionDetailResponse(
                    matchedPromotion
            );
        }


        /*
         * "지금 이벤트 있어?", "현재 프로모션 알려줘"처럼
         * 목록 자체를 묻는 질문만 전체 목록으로 보냅니다.
         */
        if (isPromotionListQuestion(normalized)) {

            return createCurrentPromotionResponse();
        }


        /*
         * 특정 이벤트의 조건/경품/내용 등을 묻는데
         * 실제 DB에서 일치하는 프로모션을 찾지 못한 경우
         * 존재하지 않는 이벤트 내용을 임의로 안내하지 않습니다.
         */
        return createPromotionNotFoundResponse();
    }


    /*
     * Gemini가 PROMOTION_CURRENT로 분류한 경우에도
     * 동일한 실제 DB 응답을 사용합니다.
     */
    public ChatbotResponse createCurrentPromotionResponse() {

        List<PromotionResponse> promotions =
                getCurrentPromotions();


        if (promotions.isEmpty()) {

            return new ChatbotResponse(
                    """
                    현재 진행 중인 프로모션이 없습니다.

                    새로운 프로모션이 등록되면 BloodOps 프로모션 화면에서 확인할 수 있습니다.
                    """.trim(),
                    "PROMOTION_EMPTY",
                    List.of(
                            new ChatbotAction(
                                    "LINK",
                                    "프로모션 보기",
                                    "/promotion"
                            )
                    ),
                    List.of(
                            "BloodOps 프로모션 DB"
                    )
            );
        }


        /*
         * 채팅창 과밀 방지를 위해 최대 3개까지만 보여줍니다.
         */
        List<PromotionResponse> visiblePromotions =
                promotions.stream()
                        .limit(3)
                        .toList();


        StringBuilder answer =
                new StringBuilder();


        answer.append("현재 진행 중인 프로모션");

        if (promotions.size() > 3) {

            answer.append(" ")
                    .append(promotions.size())
                    .append("건 중 3건");

        } else {

            answer.append(" ")
                    .append(promotions.size())
                    .append("건");
        }

        answer.append("을 안내해드릴게요.\n\n");


        for (
                int i = 0;
                i < visiblePromotions.size();
                i++
        ) {

            PromotionResponse promotion =
                    visiblePromotions.get(i);


            answer.append(i + 1)
                    .append(". ")
                    .append(promotion.title())
                    .append("\n");


            if (
                    promotion.regionDisplayName() != null
                            && !promotion.regionDisplayName().isBlank()
            ) {

                answer.append("지역: ")
                        .append(
                                promotion.regionDisplayName()
                        )
                        .append("\n");
            }


            answer.append("기간: ")
                    .append(promotion.startDate())
                    .append(" ~ ")
                    .append(promotion.endDate())
                    .append("\n");


            if (
                    promotion.summary() != null
                            && !promotion.summary().isBlank()
            ) {

                answer.append(
                                promotion.summary()
                        )
                        .append("\n");
            }


            if (
                    i
                            < visiblePromotions.size() - 1
            ) {

                answer.append("\n");
            }
        }


        return new ChatbotResponse(
                answer.toString().trim(),
                "PROMOTION_CURRENT",
                List.of(
                        new ChatbotAction(
                                "LINK",
                                "프로모션 전체 보기",
                                "/promotion"
                        )
                ),
                List.of(
                        "BloodOps 프로모션 DB"
                )
        );
    }

    /*
     * 사용자 질문에서 실제 프로모션 제목을 찾습니다.
     *
     * 완전일치가 아니라
     * 제목을 구성하는 의미 있는 단어가 질문에 포함되는지 확인합니다.
     */
    private PromotionResponse findMatchedPromotion(
            String normalizedMessage
    ) {

        List<PromotionResponse> promotions =
                getCurrentPromotions();


        PromotionResponse bestMatch = null;
        int bestScore = 0;


        for (PromotionResponse promotion : promotions) {

            String normalizedTitle =
                    promotion.title()
                            .toLowerCase(Locale.ROOT)
                            .trim();


            /*
             * 제목 전체가 질문에 포함되면
             * 가장 확실한 매칭입니다.
             */
            if (
                    normalizedMessage.contains(
                            normalizedTitle
                    )
            ) {
                return promotion;
            }


            /*
             * 제목을 공백 단위로 나누어
             * 의미 있는 단어가 얼마나 겹치는지 계산합니다.
             */
            String[] titleWords =
                    normalizedTitle.split("\\s+");

            int score = 0;


            for (String word : titleWords) {

                String cleanedWord =
                        word.replaceAll(
                                "[^가-힣a-z0-9]",
                                ""
                        );


                /*
                 * '헌혈', '이벤트'처럼 지나치게 흔한 단어만으로
                 * 특정 프로모션이 선택되는 것을 방지합니다.
                 */
                if (
                        cleanedWord.length() < 2
                                || isGenericPromotionWord(
                                cleanedWord
                        )
                ) {
                    continue;
                }


                if (
                        normalizedMessage.contains(
                                cleanedWord
                        )
                ) {
                    score++;
                }
            }


            if (score > bestScore) {

                bestScore = score;
                bestMatch = promotion;
            }
        }


        /*
         * 의미 있는 제목 단어가 최소 하나는 일치해야 합니다.
         */
        return bestScore > 0
                ? bestMatch
                : null;
    }

    private boolean isPromotionListQuestion(
            String message
    ) {

        return containsAny(
                message,
                "지금 이벤트",
                "지금 프로모션",
                "현재 이벤트",
                "현재 프로모션",
                "진행 중인 이벤트",
                "진행중인 이벤트",
                "진행 중인 프로모션",
                "진행중인 프로모션",
                "이벤트 있어",
                "프로모션 있어",
                "행사 있어",
                "이벤트 뭐 있어",
                "프로모션 뭐 있어",
                "어떤 이벤트",
                "어떤 프로모션",
                "이벤트 목록",
                "프로모션 목록",
                "전체 이벤트",
                "전체 프로모션"
        );
    }


    private ChatbotResponse createPromotionNotFoundResponse() {

        return new ChatbotResponse(
                """
                말씀하신 내용과 일치하는 현재 진행 중인 프로모션을 찾지 못했습니다.

                프로모션 이름을 다시 확인하시거나 현재 진행 중인 프로모션 목록에서 확인해주세요.
                """.trim(),
                "PROMOTION_NOT_FOUND",
                List.of(
                        new ChatbotAction(
                                "LINK",
                                "현재 프로모션 보기",
                                "/promotion"
                        )
                ),
                List.of(
                        "BloodOps 프로모션 DB"
                )
        );
    }

    private ChatbotResponse createPromotionDetailResponse(
            PromotionResponse promotion
    ) {

        StringBuilder answer =
                new StringBuilder();


        answer.append(promotion.title())
                .append("\n\n");


        if (
                promotion.regionDisplayName() != null
                        && !promotion.regionDisplayName().isBlank()
        ) {

            answer.append("지역: ")
                    .append(
                            promotion.regionDisplayName()
                    )
                    .append("\n");
        }


        answer.append("기간: ")
                .append(promotion.startDate())
                .append(" ~ ")
                .append(promotion.endDate())
                .append("\n");


        if (
                promotion.summary() != null
                        && !promotion.summary().isBlank()
        ) {

            answer.append("\n")
                    .append(
                            promotion.summary()
                    )
                    .append("\n");
        }


        /*
         * 상세 설명은 실제 DB content만 사용합니다.
         */
        if (
                promotion.content() != null
                        && !promotion.content().isBlank()
        ) {

            answer.append("\n")
                    .append(
                            promotion.content()
                    );
        }


        /*
         * 추첨형 프로모션이면 실제 응모 조건도 표시합니다.
         */
        if (
                promotion.options() != null
                        && !promotion.options().isEmpty()
        ) {

            answer.append("\n\n응모 선택 항목:");

            for (
                    PromotionResponse.OptionResponse option
                    : promotion.options()
            ) {

                answer.append("\n· ")
                        .append(
                                option.label()
                        );
            }


            answer.append(
                            "\n\n헌혈 실적 인정 기간: "
                    )
                    .append(
                            promotion.donationEligibilityStartDate()
                                    != null
                                    ? promotion.donationEligibilityStartDate()
                                    : promotion.startDate()
                    )
                    .append(" ~ ")
                    .append(
                            promotion.endDate()
                    );
        }


        return new ChatbotResponse(
                answer.toString().trim(),
                "PROMOTION_DETAIL",
                List.of(
                        new ChatbotAction(
                                "LINK",
                                "프로모션 상세 보기",
                                "/promotion/"
                                        + promotion.id()
                        )
                ),
                List.of(
                        "BloodOps 프로모션 DB"
                )
        );
    }


    /*
     * 전국 + 지역 프로모션을 합쳐 현재 공개 중인 항목을 조회합니다.
     *
     * email을 null로 전달해
     * 챗봇의 일반 프로모션 안내에서는
     * 회원 개인의 응모 자격을 임의 판정하지 않습니다.
     */
    private List<PromotionResponse> getCurrentPromotions() {

        List<PromotionResponse> promotions =
                new ArrayList<>();


        promotions.addAll(
                promotionService.published(
                        PromotionCategory.NATIONAL,
                        null
                )
        );


        promotions.addAll(
                promotionService.published(
                        PromotionCategory.REGIONAL,
                        null
                )
        );


        return promotions.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        PromotionResponse::endDate
                                )
                                .thenComparing(
                                        PromotionResponse::startDate
                                )
                )
                .toList();
    }

    private boolean isGenericPromotionWord(
            String word
    ) {

        return List.of(
                "헌혈",
                "프로모션",
                "이벤트",
                "행사",
                "응모",
                "혜택",
                "경품"
        ).contains(
                word
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
