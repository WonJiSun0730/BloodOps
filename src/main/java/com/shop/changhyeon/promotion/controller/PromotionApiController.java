package com.shop.changhyeon.promotion.controller;
import com.shop.changhyeon.promotion.constant.PromotionCategory;
import com.shop.changhyeon.promotion.dto.*;
import com.shop.changhyeon.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController @RequestMapping("/api/promotions") @RequiredArgsConstructor
public class PromotionApiController {
    private final PromotionService service;
    @GetMapping public List<PromotionResponse> list(@RequestParam(required=false) PromotionCategory category, Principal principal) { return service.published(category, principal == null ? null : principal.getName()); }
    @GetMapping("/{id}") public PromotionResponse detail(@PathVariable Long id, Principal principal) { return service.detail(id, principal == null ? null : principal.getName(), false); }
    @PostMapping("/{id}/entries") public PromotionEntryResponse enter(@PathVariable Long id, @Valid @RequestBody PromotionEntryRequest request, Authentication authentication) { return service.enter(id, request.optionId(), authentication.getName()); }
}
