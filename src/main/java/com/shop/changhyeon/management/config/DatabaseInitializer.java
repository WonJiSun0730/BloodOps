package com.shop.changhyeon.management.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        String sql = "CREATE TABLE IF NOT EXISTS donor_confirmation ("
                + "confirmation_id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(50) NOT NULL, "
                + "birth_date VARCHAR(10), "
                + "height DOUBLE, "
                + "weight DOUBLE, "
                + "blood_type VARCHAR(5), "
                + "phone VARCHAR(20), "
                + "email VARCHAR(100), "
                + "address VARCHAR(255), "
                + "signature_data LONGTEXT, "
                + "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";

        jdbcTemplate.execute(sql);
    }
}