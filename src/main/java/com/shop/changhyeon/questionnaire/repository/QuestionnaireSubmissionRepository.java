package com.shop.changhyeon.questionnaire.repository;

import com.shop.changhyeon.questionnaire.entity.QuestionnaireSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface QuestionnaireSubmissionRepository
        extends JpaRepository<QuestionnaireSubmission, Long> {

    Optional<QuestionnaireSubmission>
    findFirstByMember_EmailAndExpiresAtAfterOrderBySubmittedAtDesc(
            String email,
            LocalDateTime now
    );

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

    @Query("""
        select submission
        from QuestionnaireSubmission submission
        where submission.expiresAt > :now
          and (
            :centerId is null
            or exists (
              select reservation.id
              from Reservation reservation
              where reservation.member = submission.member
                and reservation.slot.center.id = :centerId
            )
          )
        order by submission.submittedAt desc, submission.id desc
        """)
    Page<QuestionnaireSubmission> findMedicalSubmissions(
            @Param("centerId") Long centerId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    long deleteByExpiresAtBefore(LocalDateTime now);
}
