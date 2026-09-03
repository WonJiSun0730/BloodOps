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
    public Page<QnaResponse> myQuestions(String identifier, int page) {
        Member member = findMember(identifier);
        return qnaRepository.findByMember_Id(member.getId(), pageRequest(page))
                .map(qna -> QnaResponse.from(qna, member.getId()));
    }

    @Transactional(readOnly = true)
    public Page<QnaResponse> adminList(int page, Long adminId) {
        return qnaRepository.findAll(pageRequest(page))
                .map(qna -> QnaResponse.from(qna, adminId));
    }

    @Transactional(readOnly = true)
    public QnaResponse detailForMember(Long id, String identifier, boolean admin) {
        Member member = findMember(identifier);
        Qna qna = find(id);
        if (!admin) requireOwner(qna, member);
        return QnaResponse.from(qna, member.getId());
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

    private Pageable pageRequest(int page) {
        return PageRequest.of(Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private Qna find(Long id) {
        return qnaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));
    }

    private Member findMember(String identifier) {
        return memberService.findByAuthenticationIdentifier(identifier);
    }

    private void requireOwner(Qna qna, Member member) {
        if (!qna.getMember().getId().equals(member.getId())) {
            throw new IllegalStateException("본인이 작성한 질문만 조회하거나 변경할 수 있습니다.");
        }
    }
}
