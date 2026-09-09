package com.shop.changhyeon.questionnaire.service;

import com.shop.changhyeon.identity.ResidentNumberHashService;
import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.changhyeon.management.entity.DonorConfirmation;
import com.shop.changhyeon.questionnaire.constant.QuestionnaireStatus;
import com.shop.changhyeon.questionnaire.constant.QuestionnaireVisitType;
import com.shop.changhyeon.questionnaire.dto.*;
import com.shop.changhyeon.questionnaire.entity.QuestionnaireSubmission;
import com.shop.changhyeon.questionnaire.repository.QuestionnaireReservationRepository;
import com.shop.changhyeon.questionnaire.repository.QuestionnaireSubmissionRepository;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionnaireService {

    private static final int MIN_DONATION_AGE = 16;
    private static final int MAX_DONATION_AGE = 69;
    private static final double MALE_MIN_WEIGHT_KG = 50.0;
    private static final double FEMALE_MIN_WEIGHT_KG = 45.0;
    private static final int MAX_DETAIL_LENGTH = 1000;
    private static final String NOTICE =
            "전자문진 결과는 헌혈 가능 여부를 확정하지 않습니다. 방문 시 신분증을 지참하고 의료진의 최종 확인을 받아주세요.";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String RECEIPT_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final QuestionnaireSubmissionRepository submissionRepository;
    private final MemberRepository memberRepository;
    private final CenterRepository centerRepository;
    private final QuestionnaireReservationRepository reservationRepository;
    private final ResidentNumberHashService residentNumberHashService;

    @Transactional(readOnly = true)
    public List<QuestionnaireQuestionDto> getQuestions() {
        return QuestionnaireCatalog.QUESTIONS;
    }

    public QuestionnaireSubmissionDto submit(QuestionnaireSubmitRequest request, String email) {
        Member member = findMember(email);
        LocalDateTime submittedAt = LocalDateTime.now(KOREA_ZONE);
        LocalDateTime expiresAt = submittedAt.plusDays(3);
        VisitPlan visitPlan = resolveVisitPlan(member, request, submittedAt, expiresAt);
        validateAnswers(request.answers(), request.answerDetails());
        ResidentIdentity residentIdentity = validateResidentIdentity(request.residentRegistrationNumber());
        validateWeight(request.weightKg(), residentIdentity.gender());

        String donorName = request.donorName().trim();
        String residentNumberHash = residentNumberHashService.hash(request.residentRegistrationNumber());
        Map<String, String> normalizedDetails = normalizeAnswerDetails(request.answers(), request.answerDetails());

        List<String> reasons = findReviewReasons(request.answers());
        QuestionnaireStatus status = reasons.isEmpty()
                ? QuestionnaireStatus.NO_REVIEW_NEEDED
                : QuestionnaireStatus.REVIEW_REQUIRED;

        QuestionnaireSubmission saved = submissionRepository.save(
                QuestionnaireSubmission.builder()
                        .member(member)
                        .receiptCode(createReceiptCode())
                        .donorName(donorName)
                        .residentNumberHash(residentNumberHash)
                        .residentNumberMasked(residentNumberHashService.mask(request.residentRegistrationNumber()))
                        .status(status)
                        .submittedAt(submittedAt)
                        .expiresAt(expiresAt)
                        .weightKg(request.weightKg())
                        .contactPhoneSnapshot(member == null ? null : member.getPhone())
                        .contactEmailSnapshot(member == null ? null : member.getEmail())
                        .visitType(visitPlan.visitType())
                        .reservation(visitPlan.reservation())
                        .donationCenter(visitPlan.donationCenter())
                        .answers(new LinkedHashMap<>(request.answers()))
                        .answerDetails(normalizedDetails)
                        .reviewReasons(new ArrayList<>(reasons))
                        .build()
        );

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public QuestionnaireSubmissionDto getLatest(String email) {
        return submissionRepository
                .findFirstByMember_EmailAndExpiresAtAfterOrderBySubmittedAtDesc(
                        email,
                        LocalDateTime.now()
                )
                .map(this::toDto)
                .orElse(null);
    }

    public QuestionnaireSubmissionDto findAndVerifyLatest(
            String donorName, String residentNumberHash,
            DonorConfirmation confirmation, String verifiedBy
    ) {
        return submissionRepository
                .findFirstByDonorNameAndResidentNumberHashAndExpiresAtAfterOrderBySubmittedAtDesc(
                        donorName.trim(), residentNumberHash, LocalDateTime.now())
                .map(submission -> {
                    linkGuestSubmissionToReservation(submission);
                    submission.setDonorConfirmation(confirmation);
                    submission.setVerifiedAt(LocalDateTime.now());
                    submission.setVerifiedBy(verifiedBy);
                    return toDto(submission);
                })
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public QuestionnaireSubmissionDto getVerifiedQuestionnaire(Long confirmationId) {
        return submissionRepository
                .findFirstByDonorConfirmation_ConfirmationIdOrderBySubmittedAtDesc(confirmationId)
                .map(this::toDto)
                .orElse(null);
    }

    public Page<QuestionnaireSubmissionDto> getMedicalSubmissionList(
            Long centerId,
            Pageable pageable
    ) {
        deleteExpiredSubmissions();
        return submissionRepository
                .findMedicalSubmissions(centerId, LocalDateTime.now(), pageable)
                .map(this::toDto);
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void deleteExpiredSubmissions() {
        submissionRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    private void validateAnswers(Map<String, String> answers, Map<String, String> answerDetails) {
        Set<String> expected = QuestionnaireCatalog.SAFE_ANSWERS.keySet();
        if (!answers.keySet().equals(expected)) {
            throw new IllegalArgumentException("모든 문진 항목에 한 번씩 답변해주세요.");
        }
        if (answers.values().stream().anyMatch(answer -> !QuestionnaireCatalog.YES_NO.contains(answer))) {
            throw new IllegalArgumentException("문진 답변 형식이 올바르지 않습니다.");
        }
        if (!expected.containsAll(answerDetails.keySet())) {
            throw new IllegalArgumentException("답변 상세내용 형식이 올바르지 않습니다.");
        }
        answers.forEach((questionId, answer) -> {
            String detail = answerDetails.getOrDefault(questionId, "").trim();
            if ("NO".equals(answer) && detail.isBlank()) {
                throw new IllegalArgumentException("'아니요'를 선택한 항목의 상세내용을 입력해주세요.");
            }
            if (detail.length() > MAX_DETAIL_LENGTH) {
                throw new IllegalArgumentException("상세내용은 1000자 이하로 입력해주세요.");
            }
        });
    }

    private ResidentIdentity validateResidentIdentity(String residentRegistrationNumber) {
        String digits = residentRegistrationNumber.replace("-", "");
        int genderCode = Character.digit(digits.charAt(6), 10);
        int century = switch (genderCode) {
            case 1, 2, 5, 6 -> 1900;
            case 3, 4, 7, 8 -> 2000;
            default -> throw new IllegalArgumentException("주민등록번호 뒤 첫 자리 형식을 확인해주세요.");
        };

        LocalDate birthDate;
        try {
            int year = century + Integer.parseInt(digits.substring(0, 2));
            int month = Integer.parseInt(digits.substring(2, 4));
            int day = Integer.parseInt(digits.substring(4, 6));
            birthDate = LocalDate.of(year, month, day);
        } catch (DateTimeException | NumberFormatException exception) {
            throw new IllegalArgumentException("생년월일 형식을 확인해주세요.");
        }

        LocalDate today = LocalDate.now(KOREA_ZONE);
        if (birthDate.isAfter(today)) {
            throw new IllegalArgumentException("생년월일은 오늘 이후일 수 없습니다.");
        }
        validateDonationAge(birthDate, today);
        return new ResidentIdentity(genderCode % 2 == 1 ? Gender.MALE : Gender.FEMALE);
    }

    static void validateDonationAge(LocalDate birthDate, LocalDate today) {
        int age = Period.between(birthDate, today).getYears();
        if (age < MIN_DONATION_AGE || age > MAX_DONATION_AGE) {
            throw new IllegalArgumentException(
                    "혈액관리법에 의거 만 16세 미만과 만 70세 이상인 분은 헌혈이 불가합니다."
            );
        }
    }

    private void validateWeight(Double weightKg, Gender gender) {
        if (weightKg == null || !Double.isFinite(weightKg) || weightKg > 300 || weightKg <= 0) {
            throw new IllegalArgumentException("체중은 올바른 숫자로 입력해주세요.");
        }
        if (BigDecimal.valueOf(weightKg).stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("체중은 소수점 둘째 자리까지만 입력할 수 있습니다.");
        }
        double minimumWeight = gender == Gender.MALE ? MALE_MIN_WEIGHT_KG : FEMALE_MIN_WEIGHT_KG;
        if (weightKg < minimumWeight) {
            String genderLabel = gender == Gender.MALE ? "남성" : "여성";
            throw new IllegalArgumentException(
                    genderLabel + "은 체중 " + (int) minimumWeight + "kg 이상이어야 헌혈할 수 있습니다."
            );
        }
    }

    private Map<String, String> normalizeAnswerDetails(
            Map<String, String> answers, Map<String, String> answerDetails
    ) {
        Map<String, String> normalized = new LinkedHashMap<>();
        answers.forEach((questionId, answer) -> {
            if ("NO".equals(answer)) {
                normalized.put(questionId, answerDetails.get(questionId).trim());
            }
        });
        return normalized;
    }

    private List<String> findReviewReasons(Map<String, String> answers) {
        List<String> reasons = new ArrayList<>();
        QuestionnaireCatalog.QUESTIONS.forEach(question -> {
            if (!Objects.equals(QuestionnaireCatalog.SAFE_ANSWERS.get(question.id()), answers.get(question.id()))) {
                reasons.add(question.category() + ": " + question.question());
            }
        });
        return reasons;
    }

    private VisitPlan resolveVisitPlan(
            Member member,
            QuestionnaireSubmitRequest request,
            LocalDateTime submittedAt,
            LocalDateTime expiresAt
    ) {
        QuestionnaireVisitType visitType = member == null
                ? QuestionnaireVisitType.WALK_IN
                : request.visitType();

        if (visitType == QuestionnaireVisitType.WALK_IN) {
            if (request.donationCenterId() == null) {
                throw new IllegalArgumentException("현장 방문할 헌혈 장소를 선택해주세요.");
            }
            Center center = findActiveCenter(request.donationCenterId());
            return new VisitPlan(visitType, null, center);
        }

        if (member == null) {
            throw new IllegalArgumentException("비회원은 현장 방문 장소를 선택해주세요.");
        }

        if (visitType == QuestionnaireVisitType.UNDECIDED) {
            return new VisitPlan(visitType, null, null);
        }

        if (request.reservationId() == null) {
            throw new IllegalArgumentException("전자문진 유효기간 안의 예약을 선택해주세요.");
        }

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 예약을 찾을 수 없습니다."));
        if (!Objects.equals(reservation.getMember().getId(), member.getId())) {
            throw new IllegalArgumentException("본인의 예약만 선택할 수 있습니다.");
        }
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalArgumentException("예약 대기 상태의 예약만 선택할 수 있습니다.");
        }

        if (!isReservationWithinValidity(reservation, submittedAt, expiresAt)) {
            throw new IllegalArgumentException("전자문진 유효시간 안의 예약을 선택해주세요.");
        }

        return new VisitPlan(
                visitType,
                reservation,
                reservation.getSlot().getCenter()
        );
    }

    private void linkGuestSubmissionToReservation(
            QuestionnaireSubmission submission
    ) {
        if (submission.getMember() != null
                || submission.getReservation() != null
                || submission.getDonationCenter() == null) {
            return;
        }

        List<Reservation> matches = reservationRepository
                .findByDonorNameAndResidentNumberHashAndSlot_ReservationDateAndStatusAndSlot_Center_Id(
                        submission.getDonorName(),
                        submission.getResidentNumberHash(),
                        LocalDate.now(KOREA_ZONE),
                        ReservationStatus.RESERVED,
                        submission.getDonationCenter().getId()
                );

        if (matches.size() != 1) {
            return;
        }

        Reservation reservation = matches.getFirst();
        Member member = reservation.getMember();
        submission.setMember(member);
        submission.setReservation(reservation);
        submission.setReservationAutoLinked(true);
        submission.setVisitType(QuestionnaireVisitType.RESERVATION);

        if (submission.getContactPhoneSnapshot() == null
                || submission.getContactPhoneSnapshot().isBlank()) {
            submission.setContactPhoneSnapshot(member.getPhone());
        }
        if (submission.getContactEmailSnapshot() == null
                || submission.getContactEmailSnapshot().isBlank()) {
            submission.setContactEmailSnapshot(member.getEmail());
        }
    }

    private Center findActiveCenter(Long centerId) {
        Center center = centerRepository.findById(centerId)
                .orElseThrow(() -> new IllegalArgumentException("선택한 헌혈 장소를 찾을 수 없습니다."));
        if (!center.isActive()) {
            throw new IllegalArgumentException("현재 운영 중인 헌혈 장소를 선택해주세요.");
        }
        return center;
    }

    private Member findMember(String email) {
        if (email == null || email.isBlank()) return null;
        return memberRepository.findByEmail(email);
    }

    private String createReceiptCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder code = new StringBuilder("EMI-");
            for (int index = 0; index < 10; index++) {
                code.append(RECEIPT_CHARS.charAt(RANDOM.nextInt(RECEIPT_CHARS.length())));
            }
            if (!submissionRepository.existsByReceiptCode(code.toString())) return code.toString();
        }
        throw new IllegalStateException("문진 접수번호를 생성할 수 없습니다.");
    }

    private QuestionnaireSubmissionDto toDto(QuestionnaireSubmission submission) {
        Reservation reservation = resolveReservation(submission);
        Center donationCenter = reservation != null
                ? reservation.getSlot().getCenter()
                : submission.getDonationCenter();
        QuestionnaireVisitType visitType = submission.getVisitType() != null
                ? submission.getVisitType()
                : reservation == null
                ? (donationCenter == null ? QuestionnaireVisitType.UNDECIDED : QuestionnaireVisitType.WALK_IN)
                : QuestionnaireVisitType.RESERVATION;

        return new QuestionnaireSubmissionDto(
                submission.getId(), submission.getReceiptCode(), submission.getDonorName(),
                submission.getResidentNumberMasked(), submission.getStatus(),
                submission.getSubmittedAt(), submission.getExpiresAt(), submission.getWeightKg(),
                submission.getContactPhoneSnapshot(), submission.getContactEmailSnapshot(),
                visitType,
                reservation == null ? null : reservation.getId(),
                donationCenter == null ? null : donationCenter.getId(),
                donationCenter == null ? null : donationCenter.getCenterName(),
                donationCenter == null ? null : donationCenter.getAddress(),
                reservation == null ? null : reservation.getSlot().getReservationDate(),
                reservation == null ? null : reservation.getSlot().getStartTime(),
                reservation == null ? null : reservation.getSlot().getEndTime(),
                reservation == null ? null : reservation.getStatus(),
                reservation == null || isReservationWithinValidity(
                        reservation, submission.getSubmittedAt(), submission.getExpiresAt()
                ),
                resolveReservationConnectionType(submission, reservation),
                new LinkedHashMap<>(submission.getAnswers()),
                new LinkedHashMap<>(submission.getAnswerDetails()),
                List.copyOf(submission.getReviewReasons()), submission.getVerifiedAt() != null, NOTICE
        );
    }

    private Reservation resolveReservation(QuestionnaireSubmission submission) {
        if (submission.getReservation() != null) {
            return submission.getReservation();
        }
        if (submission.getMember() == null
                || submission.getVisitType() == QuestionnaireVisitType.WALK_IN) {
            return null;
        }
        return reservationRepository
                .findByMember_IdAndStatusAndSlot_ReservationDateBetweenOrderBySlot_ReservationDateAscSlot_StartTimeAscIdAsc(
                        submission.getMember().getId(),
                        ReservationStatus.RESERVED,
                        submission.getSubmittedAt().toLocalDate(),
                        submission.getExpiresAt().toLocalDate()
                )
                .stream()
                .filter(reservation -> isReservationWithinValidity(
                        reservation, submission.getSubmittedAt(), submission.getExpiresAt()
                ))
                .findFirst()
                .orElse(null);
    }

    static boolean isReservationWithinValidity(
            Reservation reservation,
            LocalDateTime submittedAt,
            LocalDateTime expiresAt
    ) {
        LocalDateTime reservationAt = LocalDateTime.of(
                reservation.getSlot().getReservationDate(),
                reservation.getSlot().getStartTime()
        );
        return !reservationAt.isBefore(submittedAt)
                && reservationAt.isBefore(expiresAt);
    }

    private String resolveReservationConnectionType(
            QuestionnaireSubmission submission,
            Reservation reservation
    ) {
        if (reservation == null) {
            return "NONE";
        }
        if (Boolean.TRUE.equals(submission.getReservationAutoLinked())) {
            return "AUTO_IDENTITY";
        }
        if (submission.getReservation() != null) {
            return "SELECTED";
        }
        return "AUTO_CURRENT";
    }

    private enum Gender { MALE, FEMALE }

    private record ResidentIdentity(Gender gender) { }

    private record VisitPlan(
            QuestionnaireVisitType visitType,
            Reservation reservation,
            Center donationCenter
    ) { }
}
