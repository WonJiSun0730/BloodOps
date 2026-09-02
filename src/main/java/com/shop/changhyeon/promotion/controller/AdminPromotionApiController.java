package com.shop.changhyeon.promotion.controller;

import com.shop.changhyeon.promotion.dto.*;
import com.shop.changhyeon.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionApiController {
    private final PromotionService service;

    @GetMapping
    public List<PromotionResponse> list() {
        return service.adminList();
    }

    @GetMapping("/{id}")
    public PromotionResponse detail(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return service.detail(id, authentication.getName(), true);
    }

    @PostMapping
    public PromotionResponse create(
            @Valid @RequestBody PromotionRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public PromotionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody PromotionRequest request
    ) {
        return service.update(id, request);
    }

    @PutMapping("/{id}/image")
    public PromotionResponse uploadImage(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile image
    ) {
        return service.uploadImage(id, image);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
