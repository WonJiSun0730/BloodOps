package com.shop.changhyeon.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kakao_message_template")
@Getter
@NoArgsConstructor
public class KakaoMessageTemplate {

    public static final long RESERVATION_TEMPLATE_ID = 1L;

    @Id
    private Long id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(
            name = "message_text",
            nullable = false,
            length = 1000
    )
    private String messageText;

    public static KakaoMessageTemplate reservationDefault(
            String messageText
    ) {
        KakaoMessageTemplate template =
                new KakaoMessageTemplate();

        template.id = RESERVATION_TEMPLATE_ID;
        template.enabled = false;
        template.messageText = messageText;

        return template;
    }

    public void update(
            boolean enabled,
            String messageText
    ) {
        this.enabled = enabled;
        this.messageText = messageText;
    }
}
