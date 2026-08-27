package com.shop.changhyeon.notification.controller;

import com.shop.changhyeon.notification.dto.KakaoMessageTemplateResponse;
import com.shop.changhyeon.notification.dto.KakaoMessageTemplateUpdateRequest;
import com.shop.changhyeon.notification.service.KakaoMessageTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/kakao-message-template")
@RequiredArgsConstructor
public class AdminKakaoMessageTemplateController {

    private final KakaoMessageTemplateService templateService;

    @GetMapping
    public KakaoMessageTemplateResponse getTemplate() {
        return templateService.getTemplate();
    }

    @PutMapping
    public KakaoMessageTemplateResponse updateTemplate(
            @Valid @RequestBody
            KakaoMessageTemplateUpdateRequest request
    ) {
        return templateService.updateTemplate(request);
    }
}
