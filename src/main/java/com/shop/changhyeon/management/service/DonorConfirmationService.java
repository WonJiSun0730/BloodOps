package com.shop.changhyeon.management.service;

import com.shop.changhyeon.management.dto.*;
import com.shop.changhyeon.management.entity.DonorConfirmation;
import com.shop.changhyeon.management.repository.DonorConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shop.changhyeon.identity.ResidentNumberHashService;
import com.shop.changhyeon.questionnaire.dto.QuestionnaireSubmissionDto;
import com.shop.changhyeon.questionnaire.service.QuestionnaireService;

@Service @RequiredArgsConstructor
public class DonorConfirmationService {
    private final DonorConfirmationRepository repository;
    private final ResidentNumberHashService residentNumberHashService;
    private final QuestionnaireService questionnaireService;

    @Transactional(readOnly=true)
    public DonorConfirmation getConfirmation(Long id) { return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 헌혈자 확인 내역이 존재하지 않습니다. ID="+id)); }

    @Transactional(readOnly=true)
    public DonorConfirmation getConfirmation(Long id, String medicalUser) {
        DonorConfirmation confirmation = getConfirmation(id);
        assertVerificationOwner(confirmation, medicalUser);
        return confirmation;
    }

    @Transactional
    public DonorIdentityResponseDto verifyIdentity(
            DonorIdentityRequestDto dto,
            String verifiedBy
    ) {
        String digits = residentNumberHashService.normalize(
                dto.residentRegistrationNumber());
        String hash = residentNumberHashService.hash(digits);
        var existing = repository.findFirstByNameAndResidentNumberHash(
                dto.name().trim(), hash);
        if (existing.isEmpty()) {
            existing = repository.findFirstByNameAndResidentNumberHash(
                    dto.name().trim(),
                    residentNumberHashService.legacyHash(digits));
            existing.ifPresent(donor -> donor.setResidentNumberHash(hash));
        }
        DonorConfirmation donor = existing.orElseGet(() -> {
            DonorConfirmation created = new DonorConfirmation();
            created.setName(dto.name().trim());
            created.setBirthDate(residentNumberHashService.birthDate(digits));
            created.setResidentNumberHash(hash);
            created.setResidentNumberMasked(residentNumberHashService.mask(digits));
            return repository.save(created);
        });
        donor.setVerifiedBy(verifiedBy);
        QuestionnaireSubmissionDto questionnaire =
                questionnaireService.findAndVerifyLatest(
                        dto.name(), hash, donor, verifiedBy);
        if (questionnaire != null) {
            donor.setWeight(questionnaire.weightKg());
            if ((donor.getPhone() == null || donor.getPhone().isBlank()) && questionnaire.contactPhone() != null) {
                donor.setPhone(questionnaire.contactPhone());
            }
            if ((donor.getEmail() == null || donor.getEmail().isBlank()) && questionnaire.contactEmail() != null) {
                donor.setEmail(questionnaire.contactEmail());
            }
        }
        return new DonorIdentityResponseDto(
                donor.getConfirmationId(),
                existing.isPresent(),
                donor.getResidentNumberMasked(),
                DonorConfirmationResponseDto.from(donor),
                questionnaire
        );
    }

    @Transactional
    public void updateConfirmation(Long id, DonorConfirmationDto d, String medicalUser) {
        validateBirthDate(d.getDonorBirth());
        getConfirmation(id, medicalUser).updateDonorConfirmation(d.getDonorName(),d.getDonorBirth(),d.getHeight(),d.getWeight(),d.getBloodType(),d.getPhone(),d.getEmail(),d.getAddress(),d.getSignatureData());
    }

    private void assertVerificationOwner(DonorConfirmation confirmation, String medicalUser) {
        if (confirmation.getVerifiedBy() == null || !confirmation.getVerifiedBy().equals(medicalUser)) {
            throw new IllegalStateException("현재 로그인한 의료진이 신분 확인한 헌혈자만 조회할 수 있습니다.");
        }
    }

    private void validateBirthDate(String birthDate) {
        try {
            if (birthDate == null || java.time.LocalDate.parse(birthDate).isAfter(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")))) {
                throw new IllegalArgumentException("생년월일은 오늘 이후로 지정할 수 없습니다.");
            }
        } catch (java.time.format.DateTimeParseException exception) {
            throw new IllegalArgumentException("생년월일 형식을 확인해주세요.");
        }
    }

}
