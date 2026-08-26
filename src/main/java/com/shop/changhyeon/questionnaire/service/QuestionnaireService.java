package com.shop.changhyeon.questionnaire.service;

import com.shop.changhyeon.questionnaire.constant.QuestionnaireStatus;
import com.shop.changhyeon.questionnaire.dto.*;
import com.shop.changhyeon.questionnaire.entity.QuestionnaireSubmission;
import com.shop.changhyeon.questionnaire.repository.QuestionnaireSubmissionRepository;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import com.shop.changhyeon.identity.ResidentNumberHashService;
import com.shop.changhyeon.management.entity.DonorConfirmation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionnaireService {

    private static final String NOTICE =
            "전자문진 결과는 헌혈 가능 여부를 확정하지 않습니다. 방문 시 신분증을 지참하고 의료진의 최종 확인을 받아주세요.";

    private final QuestionnaireSubmissionRepository submissionRepository;
    private final MemberRepository memberRepository;
    private final ResidentNumberHashService residentNumberHashService;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String RECEIPT_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @Transactional(readOnly = true)
    public List<QuestionnaireQuestionDto> getQuestions() {
        return QuestionnaireCatalog.QUESTIONS;
    }

    public QuestionnaireSubmissionDto submit(
            QuestionnaireSubmitRequest request,
            String email
    ) {
        Member member = findMember(email);
        validateAnswers(request.answers());
        validateWeight(request.weightKg());

        String donorName = request.donorName().trim();
        String residentNumberHash = residentNumberHashService.hash(
                request.residentRegistrationNumber());

        List<String> reasons = findReviewReasons(request.answers(), request.weightKg());
        QuestionnaireStatus status = reasons.isEmpty()
                ? QuestionnaireStatus.NO_REVIEW_NEEDED
                : QuestionnaireStatus.REVIEW_REQUIRED;
        LocalDateTime submittedAt = LocalDateTime.now();

        QuestionnaireSubmission saved = submissionRepository.save(
                QuestionnaireSubmission.builder()
                        .member(member)
                        .receiptCode(createReceiptCode())
                        .donorName(donorName)
                        .residentNumberHash(residentNumberHash)
                        .residentNumberMasked(residentNumberHashService.mask(
                                request.residentRegistrationNumber()))
                        .status(status)
                        .submittedAt(submittedAt)
                        .expiresAt(submittedAt.plusDays(3))
                        .weightKg(request.weightKg())
                        .answers(new LinkedHashMap<>(request.answers()))
                        .reviewReasons(new ArrayList<>(reasons))
                        .build()
        );

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public QuestionnaireSubmissionDto getLatest(String email) {
        return submissionRepository
                .findFirstByMember_EmailOrderBySubmittedAtDesc(email)
                .map(this::toDto)
                .orElse(null);
    }

    public QuestionnaireSubmissionDto findAndVerifyLatest(
            String donorName,
            String residentNumberHash,
            DonorConfirmation confirmation,
            String verifiedBy
    ) {
        return submissionRepository
                .findFirstByDonorNameAndResidentNumberHashAndExpiresAtAfterOrderBySubmittedAtDesc(
                        donorName.trim(), residentNumberHash, LocalDateTime.now())
                .map(submission -> {
                    submission.setDonorConfirmation(confirmation);
                    submission.setVerifiedAt(LocalDateTime.now());
                    submission.setVerifiedBy(verifiedBy);
                    return toDto(submission);
                })
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public QuestionnaireSubmissionDto getVerifiedQuestionnaire(
            Long confirmationId
    ) {
        return submissionRepository
                .findFirstByDonorConfirmation_ConfirmationIdOrderBySubmittedAtDesc(
                        confirmationId)
                .map(this::toDto)
                .orElse(null);
    }

    private void validateAnswers(Map<String, String> answers) {
        Set<String> expected = QuestionnaireCatalog.SAFE_ANSWERS.keySet();
        if (!answers.keySet().equals(expected)) {
            throw new IllegalArgumentException("모든 문진 항목에 한 번씩 답변해주세요.");
        }
        boolean invalid = answers.values().stream()
                .anyMatch(answer -> !QuestionnaireCatalog.YES_NO.contains(answer));
        if (invalid) {
            throw new IllegalArgumentException("문진 답변 형식이 올바르지 않습니다.");
        }
    }

    private void validateWeight(Double weightKg) {
        if (weightKg == null || !Double.isFinite(weightKg) || weightKg < 20 || weightKg > 300) {
            throw new IllegalArgumentException("체중은 20kg 이상 300kg 이하로 입력해주세요.");
        }
    }

    private List<String> findReviewReasons(Map<String, String> answers, double weightKg) {
        List<String> reasons = new ArrayList<>();
        QuestionnaireCatalog.QUESTIONS.forEach(question -> {
            if (!Objects.equals(
                    QuestionnaireCatalog.SAFE_ANSWERS.get(question.id()),
                    answers.get(question.id()))) {
                reasons.add(question.category() + ": " + question.question());
            }
        });
        if (weightKg < 45) {
            reasons.add("체중: 헌혈 가능 체중 기준을 현장에서 확인해야 합니다.");
        }
        return reasons;
    }

    private Member findMember(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        Member member = memberRepository.findByEmail(email);
        return member;
    }

    private String createReceiptCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder code = new StringBuilder("EMI-");
            for (int index = 0; index < 10; index++) {
                code.append(RECEIPT_CHARS.charAt(
                        RANDOM.nextInt(RECEIPT_CHARS.length())));
            }
            if (!submissionRepository.existsByReceiptCode(code.toString())) {
                return code.toString();
            }
        }
        throw new IllegalStateException("문진 접수번호를 생성할 수 없습니다.");
    }

    private QuestionnaireSubmissionDto toDto(QuestionnaireSubmission submission) {
        return new QuestionnaireSubmissionDto(
                submission.getId(), submission.getReceiptCode(),
                submission.getDonorName(),
                submission.getResidentNumberMasked(),
                submission.getStatus(),
                submission.getSubmittedAt(), submission.getExpiresAt(),
                submission.getWeightKg(),
                new LinkedHashMap<>(submission.getAnswers()),
                List.copyOf(submission.getReviewReasons()),
                submission.getVerifiedAt() != null,
                NOTICE);
    }
}
