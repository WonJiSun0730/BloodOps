package com.shop.changhyeon.qna.service;

import com.shop.changhyeon.qna.dto.*;
import com.shop.changhyeon.qna.entity.Qna;
import com.shop.changhyeon.qna.repository.QnaRepository;
import com.shop.member.entity.Member;
import com.shop.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QnaService {
    private final QnaRepository qnaRepository;
    private final MemberService memberService;

    @Transactional(readOnly = true)
    public Page<QnaResponse> list(String keyword, int page, Long viewerId) {
        String normalized = keyword == null ? "" : keyword.trim();
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return qnaRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                        normalized, normalized, pageable
                )
                .map(qna -> QnaResponse.from(qna, viewerId));
    }

    @Transactional(readOnly = true)
    public QnaResponse detail(Long id, Long viewerId) {
        return QnaResponse.from(find(id), viewerId);
    }

    public QnaResponse create(QnaRequest request, String identifier) {
        Member member = findMember(identifier);
        return QnaResponse.from(
                qnaRepository.save(new Qna(member, request.title(), request.content())),
                member.getId()
        );
    }

    public QnaResponse update(Long id, QnaRequest request, String identifier) {
        Member member = findMember(identifier);
        Qna qna = find(id);
        requireOwner(qna, member);
        if (qna.isAnswered()) {
            throw new IllegalStateException("답변이 완료된 질문은 수정할 수 없습니다.");
        }
        qna.updateQuestion(request.title(), request.content());
        return QnaResponse.from(qna, member.getId());
    }

    public void delete(Long id, String identifier, boolean admin) {
        Member member = findMember(identifier);
        Qna qna = find(id);
        if (!admin) requireOwner(qna, member);
        qnaRepository.delete(qna);
    }

    public QnaResponse answer(Long id, QnaAnswerRequest request, Long viewerId) {
        Qna qna = find(id);
        qna.answer(request.answer());
        return QnaResponse.from(qna, viewerId);
    }

    private Qna find(Long id) {
        return qnaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));
    }

    private Member findMember(String identifier) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }
        return member;
    }

    private void requireOwner(Qna qna, Member member) {
        if (!qna.getMember().getId().equals(member.getId())) {
            throw new IllegalStateException("본인이 작성한 질문만 변경할 수 있습니다.");
        }
    }
}
