package com.shop.condition.dto;

import com.shop.condition.constant.OverallCondition;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ConditionCheckResponseDto {

    private Long id;
    private Long donationHistoryId;

    private OverallCondition overallCondition;

    private boolean dizziness;
    private boolean nausea;
    private boolean bleedingOrBruising;
    private boolean armPain;
    private boolean fainting;
    private String otherSymptom;

    private List<String> guidance;

    private boolean rewardGranted;
    private Integer rewardPoints;

    private LocalDateTime createdAt;
}