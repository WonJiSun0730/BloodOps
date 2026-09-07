package com.shop.changhyeon.questionnaire.controller;

import com.shop.changhyeon.questionnaire.dto.QuestionnaireSubmissionDto;
import com.shop.changhyeon.questionnaire.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical/questionnaires")
@RequiredArgsConstructor
public class MedicalQuestionnaireApiController {
    private static final int QUESTIONNAIRE_PAGE_SIZE = 10;

    private final QuestionnaireService questionnaireService;

    @GetMapping
    public Page<QuestionnaireSubmissionDto> getSubmissions(
            @RequestParam(required = false) Long centerId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return questionnaireService.getMedicalSubmissionList(
                centerId,
                PageRequest.of(Math.max(page, 0), QUESTIONNAIRE_PAGE_SIZE)
        );
    }
}
