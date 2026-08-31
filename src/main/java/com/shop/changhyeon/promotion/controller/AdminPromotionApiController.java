package com.shop.changhyeon.promotion.controller;
import com.shop.changhyeon.promotion.dto.*;
import com.shop.changhyeon.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/admin/promotions") @RequiredArgsConstructor
public class AdminPromotionApiController {
    private final PromotionService service;
    @GetMapping public List<PromotionResponse> list() { return service.adminList(); }
    @PostMapping public PromotionResponse create(@Valid @RequestBody PromotionRequest request) { return service.create(request); }
    @PutMapping("/{id}") public PromotionResponse update(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
