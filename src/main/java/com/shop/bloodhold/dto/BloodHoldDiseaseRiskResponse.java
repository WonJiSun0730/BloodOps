package com.shop.bloodhold.dto;

public record BloodHoldDiseaseRiskResponse(
        String diseaseName,
        String visitRisk,
        String reason,
        boolean aiAvailable
) {
}