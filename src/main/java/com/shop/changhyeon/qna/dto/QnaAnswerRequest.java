package com.shop.changhyeon.qna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QnaAnswerRequest(
        @NotBlank(message = "답변 내용을 입력해주세요.")
        @Size(max = 5000, message = "답변은 5,000자 이내로 입력해주세요.")
        String answer
) {}
