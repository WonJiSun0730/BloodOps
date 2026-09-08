package com.shop.bloodhold.controller;

import com.shop.bloodhold.dto.BloodHoldDiseaseRiskRequest;
import com.shop.bloodhold.dto.BloodHoldDiseaseRiskResponse;
import com.shop.bloodhold.service.BloodHoldDiseaseRiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blood-hold")
@RequiredArgsConstructor
public class BloodHoldDiseaseRiskController {

    private final BloodHoldDiseaseRiskService bloodHoldDiseaseRiskService;

    @PostMapping("/disease-risk")
    public ResponseEntity<BloodHoldDiseaseRiskResponse> classifyDiseaseRisk(
            @RequestBody BloodHoldDiseaseRiskRequest request
    ) {

        BloodHoldDiseaseRiskService.DiseaseRiskResult result =
                bloodHoldDiseaseRiskService.classifyDiseaseRisk(
                        request.diseaseName()
                );

        BloodHoldDiseaseRiskResponse response =
                new BloodHoldDiseaseRiskResponse(
                        result.diseaseName(),
                        result.visitRisk().name(),
                        result.reason(),
                        result.aiAvailable()
                );

        return ResponseEntity.ok(response);
    }
}
