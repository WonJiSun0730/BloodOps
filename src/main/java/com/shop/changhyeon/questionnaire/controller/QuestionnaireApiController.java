package com.shop.changhyeon.questionnaire.controller;

import com.shop.changhyeon.questionnaire.dto.*;
import com.shop.changhyeon.questionnaire.service.QuestionnaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questionnaires")
@RequiredArgsConstructor
public class QuestionnaireApiController {

    private final QuestionnaireService questionnaireService;

    @GetMapping("/questions")
    public List<QuestionnaireQuestionDto> getQuestions() {
        return questionnaireService.getQuestions();
    }

    @PostMapping
    public QuestionnaireSubmissionDto submit(
            @Valid @RequestBody QuestionnaireSubmitRequest request,
            Authentication authentication
    ) {
        String email = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())
                ? authentication.getName()
                : null;
        return questionnaireService.submit(request, email);
    }

    @GetMapping("/me/latest")
    public ResponseEntity<QuestionnaireSubmissionDto> getLatest(Authentication authentication) {
        QuestionnaireSubmissionDto latest =
                questionnaireService.getLatest(requireEmail(authentication));
        return latest == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(latest);
    }

    private String requireEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return authentication.getName();
    }
}
