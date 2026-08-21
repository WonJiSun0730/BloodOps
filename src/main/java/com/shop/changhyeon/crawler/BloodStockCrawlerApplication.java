package com.shop.changhyeon.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BloodStockCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BloodStockCrawlerApplication.class, args);
    }
}