package com.shop.changhyeon.identity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.security.MessageDigest;

@Service
public class ResidentNumberHashService {

    private static final String ALGORITHM = "HmacSHA256";
    private final byte[] secretKey;

    public ResidentNumberHashService(
            @Value("${app.identity-hash-pepper}") String pepper
    ) {
        if (pepper == null || pepper.isBlank()) {
            throw new IllegalStateException("IDENTITY_HASH_PEPPER 환경변수를 설정해주세요.");
        }
        this.secretKey = pepper.getBytes(StandardCharsets.UTF_8);
    }

    public String normalize(String residentRegistrationNumber) {
        String digits = residentRegistrationNumber == null
                ? ""
                : residentRegistrationNumber.replaceAll("\\D", "");
        if (!digits.matches("\\d{13}")) {
            throw new IllegalArgumentException("주민등록번호 형식을 확인해주세요.");
        }
        return digits;
    }

    public String hash(String residentRegistrationNumber) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secretKey, ALGORITHM));
            return HexFormat.of().formatHex(
                    mac.doFinal(normalize(residentRegistrationNumber)
                            .getBytes(StandardCharsets.UTF_8))
            );
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("신분 식별값 생성에 실패했습니다.", exception);
        }
    }

    public String legacyHash(String residentRegistrationNumber) {
        try {
            String normalized = normalize(residentRegistrationNumber);
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(
                            (new String(secretKey, StandardCharsets.UTF_8)
                                    + ":" + normalized)
                                    .getBytes(StandardCharsets.UTF_8))
            );
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("기존 신분 식별값 생성에 실패했습니다.", exception);
        }
    }

    public String mask(String residentRegistrationNumber) {
        String digits = normalize(residentRegistrationNumber);
        return digits.substring(0, 6) + "-" + digits.charAt(6) + "******";
    }

    public String birthDate(String residentRegistrationNumber) {
        String digits = normalize(residentRegistrationNumber);
        int year = Integer.parseInt(digits.substring(0, 2));
        int genderCode = digits.charAt(6) - '0';
        int century = switch (genderCode) {
            case 1, 2, 5, 6 -> 1900;
            case 3, 4, 7, 8 -> 2000;
            default -> throw new IllegalArgumentException("주민등록번호 뒷자리 형식을 확인해주세요.");
        };
        return "%04d-%s-%s".formatted(
                century + year,
                digits.substring(2, 4),
                digits.substring(4, 6)
        );
    }
}
