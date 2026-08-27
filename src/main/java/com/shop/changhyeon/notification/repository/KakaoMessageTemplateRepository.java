package com.shop.changhyeon.notification.repository;

import com.shop.changhyeon.notification.entity.KakaoMessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KakaoMessageTemplateRepository
        extends JpaRepository<KakaoMessageTemplate, Long> {
}
