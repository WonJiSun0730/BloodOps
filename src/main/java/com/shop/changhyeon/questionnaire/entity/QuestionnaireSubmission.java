package com.shop.changhyeon.questionnaire.entity;

import com.shop.changhyeon.questionnaire.constant.QuestionnaireStatus;
import com.shop.common.entity.BaseEntity;
import com.shop.member.entity.Member;
import com.shop.changhyeon.management.entity.DonorConfirmation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "questionnaire_submission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "receipt_code", nullable = false, unique = true, length = 20)
    private String receiptCode;

    @Column(name = "donor_name", nullable = false, length = 100)
    private String donorName;

    @Column(name = "resident_number_hash", nullable = false, length = 64)
    private String residentNumberHash;

    @Column(name = "resident_number_masked", nullable = false, length = 14)
    private String residentNumberMasked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmation_id")
    private DonorConfirmation donorConfirmation;

    private LocalDateTime verifiedAt;

    @Column(length = 255)
    private String verifiedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionnaireStatus status;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Double weightKg;

    @Column(name = "contact_phone_snapshot", length = 30)
    private String contactPhoneSnapshot;

    @Column(name = "contact_email_snapshot", length = 255)
    private String contactEmailSnapshot;

    @ElementCollection
    @CollectionTable(name = "questionnaire_answer", joinColumns = @JoinColumn(name = "submission_id"))
    @MapKeyColumn(name = "question_id", length = 80)
    @Column(name = "answer", nullable = false, length = 10)
    @Builder.Default
    private Map<String, String> answers = new LinkedHashMap<>();

    @ElementCollection
    @CollectionTable(name = "questionnaire_answer_detail", joinColumns = @JoinColumn(name = "submission_id"))
    @MapKeyColumn(name = "question_id", length = 80)
    @Column(name = "detail", nullable = false, length = 1000)
    @Builder.Default
    private Map<String, String> answerDetails = new LinkedHashMap<>();

    @ElementCollection
    @CollectionTable(name = "questionnaire_review_reason", joinColumns = @JoinColumn(name = "submission_id"))
    @OrderColumn(name = "reason_order")
    @Column(name = "reason", nullable = false, length = 500)
    @Builder.Default
    private List<String> reviewReasons = new ArrayList<>();
}
