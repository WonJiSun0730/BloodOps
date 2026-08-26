package com.shop.changhyeon.questionnaire.repository;

import com.shop.changhyeon.questionnaire.entity.QuestionnaireSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDateTime;

public interface QuestionnaireSubmissionRepository
        extends JpaRepository<QuestionnaireSubmission, Long> {

    Optional<QuestionnaireSubmission>
    findFirstByMember_EmailOrderBySubmittedAtDesc(String email);

    Optional<QuestionnaireSubmission>
    findFirstByDonorNameAndResidentNumberHashAndExpiresAtAfterOrderBySubmittedAtDesc(
            String donorName,
            String residentNumberHash,
            LocalDateTime now
    );

    boolean existsByReceiptCode(String receiptCode);

    Optional<QuestionnaireSubmission>
    findFirstByDonorConfirmation_ConfirmationIdOrderBySubmittedAtDesc(
            Long confirmationId
    );
}
