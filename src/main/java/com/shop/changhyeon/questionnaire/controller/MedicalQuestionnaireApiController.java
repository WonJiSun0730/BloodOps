package com.shop.changhyeon.questionnaire.controller;

import com.shop.changhyeon.questionnaire.dto.QuestionnaireSubmissionDto;
import com.shop.changhyeon.questionnaire.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medical/questionnaires")
@RequiredArgsConstructor
public class MedicalQuestionnaireApiController {
    private final QuestionnaireService questionnaireService;

    @GetMapping
    public List<QuestionnaireSubmissionDto> getSubmissions() {
        return questionnaireService.getMedicalSubmissionList();
    }
}
