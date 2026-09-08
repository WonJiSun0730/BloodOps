package com.shop.condition.event;

public record ConditionCheckNotificationEvent(
        Long donationHistoryId,
        String phone
) {
}