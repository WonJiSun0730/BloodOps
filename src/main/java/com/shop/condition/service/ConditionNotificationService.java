package com.shop.condition.service;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConditionNotificationService {

    private final boolean enabled;
    private final String apiKey;
    private final String apiSecret;
    private final String sender;
    private final String frontendUrl;

    public ConditionNotificationService(
            @Value("${solapi.enabled:false}") boolean enabled,
            @Value("${solapi.api-key:}") String apiKey,
            @Value("${solapi.api-secret:}") String apiSecret,
            @Value("${solapi.sender:}") String sender,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl
    ) {
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.sender = sender;
        this.frontendUrl = frontendUrl;
    }

    public void sendConditionCheckMessage(
            Long donationHistoryId,
            String memberPhone
    ) {
        if (!enabled) {
            log.info(
                    "[컨디션 체크 문자] SOLAPI 비활성화 - donationHistoryId={}",
                    donationHistoryId
            );
            return;
        }

        String phone = normalizePhone(memberPhone);

        if (phone == null) {
            log.warn(
                    "[컨디션 체크 문자] 휴대전화 번호 없음 - donationHistoryId={}",
                    donationHistoryId
            );
            return;
        }

        if (
                apiKey.isBlank()
                        || apiSecret.isBlank()
                        || sender.isBlank()
        ) {
            log.warn(
                    "[컨디션 체크 문자] SOLAPI 환경변수 미설정 - donationHistoryId={}",
                    donationHistoryId
            );
            return;
        }

        try {
            DefaultMessageService messageService =
                    SolapiClient.INSTANCE.createInstance(
                            apiKey,
                            apiSecret
                    );

            Message message = new Message();

            message.setFrom(
                    normalizePhone(sender)
            );

            message.setTo(phone);

            message.setText(
                    createMessage(donationHistoryId)
            );

            messageService.send(
                    message,
                    null
            );

            log.info(
                    "[컨디션 체크 문자] 발송 요청 완료 - donationHistoryId={}",
                    donationHistoryId
            );

        } catch (Exception exception) {
            log.error(
                    "[컨디션 체크 문자] 발송 실패 - donationHistoryId={}",
                    donationHistoryId,
                    exception
            );
        }
    }

    private String createMessage(
            Long donationHistoryId
    ) {
        String baseUrl =
                frontendUrl.endsWith("/")
                        ? frontendUrl.substring(
                        0,
                        frontendUrl.length() - 1
                )
                        : frontendUrl;

        String conditionCheckUrl =
                baseUrl
                        + "/condition-check/"
                        + donationHistoryId;

        return """
                [BloodOps]
                헌혈하시느라 수고하셨습니다 😊
                지금 몸 상태는 어떠신가요?

                헌혈 후 컨디션 체크:
                %s
                """.formatted(conditionCheckUrl);
    }

    private String normalizePhone(
            String phone
    ) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        String normalized =
                phone.replaceAll(
                        "[^0-9]",
                        ""
                );

        return normalized.isBlank()
                ? null
                : normalized;
    }
}