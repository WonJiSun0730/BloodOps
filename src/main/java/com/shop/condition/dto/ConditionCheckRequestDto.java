package com.shop.condition.dto;

import com.shop.condition.constant.OverallCondition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConditionCheckRequestDto {

    @NotNull
    private OverallCondition overallCondition;

    private boolean dizziness;
    private boolean nausea;
    private boolean bleedingOrBruising;
    private boolean armPain;
    private boolean fainting;

    @Size(max = 500)
    private String otherSymptom;
}