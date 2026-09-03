package com.shop.changhyeon.qna.controller;

import com.shop.changhyeon.qna.dto.QnaResponse;
import com.shop.changhyeon.qna.service.QnaService;
import com.shop.member.entity.Member;
import com.shop.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/qna")
@RequiredArgsConstructor
public class AdminQnaController {
    private final QnaService qnaService;
    private final MemberService memberService;

    @GetMapping
    public Page<QnaResponse> list(
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication
    ) {
        Member admin = memberService.findByAuthenticationIdentifier(authentication.getName());
        return qnaService.adminList(page, admin.getId());
    }
}
