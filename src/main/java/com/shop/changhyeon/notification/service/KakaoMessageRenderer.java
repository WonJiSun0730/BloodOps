package com.shop.changhyeon.notification.service;

import com.shop.reservation.constant.DonationType;
import com.shop.reservation.dto.AdminReservationResponseDto;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class KakaoMessageRenderer {

    public static final int MAXIMUM_TEXT_LENGTH = 200;

    public static final String DEFAULT_MESSAGE = """
            #{예약자명}님 헌혈예약 감사드립니다.
            ■ 일시: #{예약일시}
            ■ 예약 헌혈 종류: #{헌혈종류}
            ■ 장소: #{헌혈장소}
            ■ 헌혈 시 사진이 부착된 신분증을 지참해주세요.
            ※ 예약시간 경과 시 예약이 취소될 수 있습니다.
            """.strip();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    public String render(
            String template,
            AdminReservationResponseDto reservation
    ) {
        String reservationDateTime =
                reservation.getReservationDate()
                        .format(DATE_FORMATTER)
                        + " "
                        + reservation.getStartTime()
                        .format(TIME_FORMATTER);

        return render(
                template,
                reservation.getMemberName(),
                reservationDateTime,
                donationTypeLabel(
                        reservation.getDonationType()
                ),
                reservation.getCenterName()
        );
    }

    public String preview(String template) {
        return render(
                template,
                "홍길동",
                "2026-09-13 16:30",
                "혈소판",
                "헌혈의집 구월센터"
        );
    }

    public int length(String text) {
        return text.codePointCount(
                0,
                text.length()
        );
    }

    public void validateLength(String renderedText) {
        if (length(renderedText) > MAXIMUM_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                    "예약정보가 적용된 메시지는 200자를 초과할 수 없습니다."
            );
        }
    }

    private String render(
            String template,
            String memberName,
            String reservationDateTime,
            String donationType,
            String centerName
    ) {
        return template
                .replace("#{예약자명}", safe(memberName))
                .replace("#{예약일시}", safe(reservationDateTime))
                .replace("#{헌혈종류}", safe(donationType))
                .replace("#{헌혈장소}", safe(centerName));
    }

    private String donationTypeLabel(
            DonationType donationType
    ) {
        return switch (donationType) {
            case WHOLE_BLOOD -> "전혈";
            case PLATELET -> "혈소판";
            case PLASMA -> "혈장";
            case PLATELET_PLASMA -> "혈소판혈장";
        };
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}
