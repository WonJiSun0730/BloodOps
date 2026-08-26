package com.shop.changhyeon.questionnaire.dto;

import java.util.List;

public record QuestionnaireQuestionDto(
        String id,
        String category,
        String question,
        String helpText,
        List<String> options
) {
}
