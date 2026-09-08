package com.shop.condition.event;

import com.shop.condition.service.ConditionNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ConditionCheckNotificationListener {

    private final ConditionNotificationService conditionNotificationService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleConditionCheckNotification(
            ConditionCheckNotificationEvent event
    ) {
        conditionNotificationService
                .sendConditionCheckMessage(
                        event.donationHistoryId(),
                        event.phone()
                );
    }
}