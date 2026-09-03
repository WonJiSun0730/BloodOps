package com.shop.changhyeon.qna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QnaRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 150, message = "제목은 150자 이내로 입력해주세요.")
        String title,

        @NotBlank(message = "문의 내용을 입력해주세요.")
        @Size(max = 5000, message = "문의 내용은 5,000자 이내로 입력해주세요.")
        String content
) {}
