package com.shop.changhyeon.notification.service;

import com.shop.changhyeon.notification.dto.KakaoMessageTemplateResponse;
import com.shop.changhyeon.notification.dto.KakaoMessageTemplateUpdateRequest;
import com.shop.changhyeon.notification.entity.KakaoMessageTemplate;
import com.shop.changhyeon.notification.repository.KakaoMessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KakaoMessageTemplateService {

    private final KakaoMessageTemplateRepository repository;
    private final KakaoMessageRenderer renderer;

    @Transactional(readOnly = true)
    public KakaoMessageTemplateResponse getTemplate() {
        KakaoMessageTemplate template =
                repository.findById(
                                KakaoMessageTemplate.RESERVATION_TEMPLATE_ID
                        )
                        .orElseGet(() ->
                                KakaoMessageTemplate.reservationDefault(
                                        KakaoMessageRenderer.DEFAULT_MESSAGE
                                )
                        );

        return response(template);
    }

    public KakaoMessageTemplateResponse updateTemplate(
            KakaoMessageTemplateUpdateRequest request
    ) {
        String messageText = request.messageText().strip();
        String previewText = renderer.preview(messageText);

        renderer.validateLength(previewText);

        KakaoMessageTemplate template =
                repository.findById(
                                KakaoMessageTemplate.RESERVATION_TEMPLATE_ID
                        )
                        .orElseGet(() ->
                                KakaoMessageTemplate.reservationDefault(
                                        KakaoMessageRenderer.DEFAULT_MESSAGE
                                )
                        );

        template.update(
                request.enabled(),
                messageText
        );

        return response(
                repository.save(template)
        );
    }

    @Transactional(readOnly = true)
    public KakaoMessageTemplate getTemplateEntity() {
        return repository.findById(
                        KakaoMessageTemplate.RESERVATION_TEMPLATE_ID
                )
                .orElseGet(() ->
                        KakaoMessageTemplate.reservationDefault(
                                KakaoMessageRenderer.DEFAULT_MESSAGE
                        )
                );
    }

    private KakaoMessageTemplateResponse response(
            KakaoMessageTemplate template
    ) {
        String previewText =
                renderer.preview(template.getMessageText());

        return new KakaoMessageTemplateResponse(
                template.isEnabled(),
                template.getMessageText(),
                previewText,
                renderer.length(previewText),
                KakaoMessageRenderer.MAXIMUM_TEXT_LENGTH
        );
    }
}
