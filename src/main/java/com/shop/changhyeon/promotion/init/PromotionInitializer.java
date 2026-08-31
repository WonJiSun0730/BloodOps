package com.shop.changhyeon.promotion.init;
import com.shop.changhyeon.promotion.constant.*;
import com.shop.changhyeon.promotion.dto.PromotionRequest;
import com.shop.changhyeon.promotion.repository.PromotionRepository;
import com.shop.changhyeon.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component @RequiredArgsConstructor
public class PromotionInitializer implements ApplicationRunner {
    private static final String TITLE = "매월 13일 헌혈 포인트 데이";
    private final PromotionRepository repository;
    private final PromotionService service;
    @Override public void run(ApplicationArguments args) {
        if (repository.existsByTitle(TITLE)) return;
        service.create(new PromotionRequest(PromotionCategory.NATIONAL, PromotionType.INFORMATION, null, TITLE, "매월 13일 헌혈을 완료하면 기본 포인트에 5,000P를 더 드립니다.", "BloodOps가 매월 13일의 헌혈 참여를 응원합니다. 예약 후 당일 헌혈을 완료하면 헌혈 종류별 기본 포인트와 추가 5,000P가 한 번에 지급됩니다.", "/promotion/monthly-13-bonus.png", LocalDate.of(2026,1,1), LocalDate.of(2099,12,31), true, List.of()));
    }
}
