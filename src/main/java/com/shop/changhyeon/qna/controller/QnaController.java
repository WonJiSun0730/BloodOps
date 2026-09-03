package com.shop.changhyeon.qna.controller;

import com.shop.changhyeon.qna.dto.*;
import com.shop.changhyeon.qna.service.QnaService;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaController {
    private final QnaService qnaService;
    private final MemberRepository memberRepository;

    @GetMapping
    public Page<QnaResponse> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication
    ) {
        return qnaService.list(keyword, page, viewerId(authentication));
    }

    @GetMapping("/{id}")
    public QnaResponse detail(@PathVariable Long id, Authentication authentication) {
        return qnaService.detail(id, viewerId(authentication));
    }

    @PostMapping
    public QnaResponse create(
            @Valid @RequestBody QnaRequest request,
            Authentication authentication
    ) {
        requireLogin(authentication);
        return qnaService.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public QnaResponse update(
            @PathVariable Long id,
            @Valid @RequestBody QnaRequest request,
            Authentication authentication
    ) {
        requireLogin(authentication);
        return qnaService.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        requireLogin(authentication);
        qnaService.delete(id, authentication.getName(), isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/answer")
    public QnaResponse answer(
            @PathVariable Long id,
            @Valid @RequestBody QnaAnswerRequest request,
            Authentication authentication
    ) {
        requireAdmin(authentication);
        return qnaService.answer(id, request, viewerId(authentication));
    }

    private Long viewerId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) return null;
        Member member = memberRepository.findByEmail(authentication.getName());
        return member == null ? null : member.getId();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private void requireLogin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new IllegalStateException("로그인 후 이용해주세요.");
        }
    }

    private void requireAdmin(Authentication authentication) {
        requireLogin(authentication);
        if (!isAdmin(authentication)) {
            throw new IllegalStateException("관리자만 답변을 등록할 수 있습니다.");
        }
    }
}
