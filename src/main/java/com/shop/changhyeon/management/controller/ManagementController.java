package com.shop.changhyeon.management.controller;

import com.shop.changhyeon.management.dto.DonorConfirmationDto;
import com.shop.changhyeon.management.entity.DonorConfirmation;
import com.shop.changhyeon.management.service.DonorConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/medical")
@RequiredArgsConstructor
public class ManagementController {

    private final DonorConfirmationService confirmationService;

    @GetMapping("/donor_check/{confirmationId}")
    public String showDonorCheckPage(@PathVariable Long confirmationId, Model model) {
        DonorConfirmation confirmation = confirmationService.getConfirmation(confirmationId);
        model.addAttribute("confirmation", confirmation);
        return "changhyeon/management/donor_check";
    }

    @PostMapping("/donor_check/{confirmationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateDonorCheck(
            @PathVariable Long confirmationId,
            @RequestBody DonorConfirmationDto dto) {
        confirmationService.updateConfirmation(confirmationId, dto);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "헌혈자 정보 및 서명이 성공적으로 저장되었습니다.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/blood_units_management")
    public String showBloodUnitsManagementPage() {
        return "changhyeon/management/blood_units_management";
    }
}
