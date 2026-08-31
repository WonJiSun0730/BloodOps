package com.shop.changhyeon.promotion.controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {PromotionApiController.class, AdminPromotionApiController.class})
public class PromotionExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, DataIntegrityViolationException.class})
    public ResponseEntity<Map<String,String>> badRequest(RuntimeException e) { String message = e instanceof DataIntegrityViolationException ? "이미 응모했거나 다른 데이터에서 사용 중인 항목입니다." : e.getMessage(); return ResponseEntity.badRequest().body(Map.of("message", message)); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> validation(MethodArgumentNotValidException e) { return ResponseEntity.badRequest().body(Map.of("message", "프로모션 입력값을 확인해주세요.")); }
}
