package com.shop.changhyeon.questionnaire.dto;

import com.shop.changhyeon.questionnaire.constant.QuestionnaireVisitType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record QuestionnaireSubmitRequest(
        @NotBlank(message = "성명을 입력해주세요.")
        @Pattern(
                regexp = "^(?:[가-힣]{2,20}|[A-Za-z]+(?:[ '-][A-Za-z]+)*)$",
                message = "성명은 한글 2~20자 또는 올바른 영문 이름 형식으로 입력해주세요."
        )
        String donorName,

        @NotBlank(message = "주민등록번호를 입력해주세요.")
        @Pattern(
                regexp = "\\d{6}-?\\d{7}",
                message = "주민등록번호 형식을 확인해주세요."
        )
        String residentRegistrationNumber,

        @NotNull(message = "방문 방식을 선택해주세요.")
        QuestionnaireVisitType visitType,

        Long reservationId,

        Long donationCenterId,

        @NotEmpty(message = "모든 문진 항목에 답변해주세요.")
        Map<String, String> answers,

        @NotNull(message = "답변 상세내용을 입력해주세요.")
        Map<String, String> answerDetails,

        @NotNull(message = "체중을 입력해주세요.")
        Double weightKg,

        @AssertTrue(message = "개인정보 수집 및 문진 작성에 동의해주세요.")
        boolean consented
) {
}
