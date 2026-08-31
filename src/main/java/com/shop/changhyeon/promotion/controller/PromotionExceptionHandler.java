package com.shop.changhyeon.promotion.controller;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {PromotionApiController.class, AdminPromotionApiController.class})
public class PromotionExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String,String>> badRequest(RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> validation(MethodArgumentNotValidException e) { return ResponseEntity.badRequest().body(Map.of("message", "프로모션 입력값을 확인해주세요.")); }
}
