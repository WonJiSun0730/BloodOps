package com.shop.changhyeon.promotion.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {
        PromotionApiController.class,
        AdminPromotionApiController.class
})
public class PromotionExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<Map<String, String>> badRequest(RuntimeException exception) {
        String message = exception instanceof DataIntegrityViolationException
                ? "이미 처리된 요청이거나 저장할 수 없는 데이터입니다. 입력 내용을 확인해주세요."
                : exception.getMessage();

        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            String field = normalizeFieldName(fieldError.getField());
            errors.putIfAbsent(field, fieldError.getDefaultMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "입력하지 않았거나 형식이 올바르지 않은 항목을 확인해주세요.");
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    private String normalizeFieldName(String field) {
        return field.startsWith("options[") ? "options" : field;
    }
}
