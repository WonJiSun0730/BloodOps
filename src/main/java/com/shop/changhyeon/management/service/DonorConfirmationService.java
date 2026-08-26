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
        QuestionnaireSubmissionDto questionnaire =
                questionnaireService.findAndVerifyLatest(
                        dto.name(), hash, donor, verifiedBy);
        return new DonorIdentityResponseDto(
                donor.getConfirmationId(),
                existing.isPresent(),
                donor.getResidentNumberMasked(),
                DonorConfirmationResponseDto.from(donor),
                questionnaire
        );
    }

    @Transactional
    public void updateConfirmation(Long id, DonorConfirmationDto d) { getConfirmation(id).updateDonorConfirmation(d.getDonorName(),d.getDonorBirth(),d.getHeight(),d.getWeight(),d.getBloodType(),d.getPhone(),d.getEmail(),d.getAddress(),d.getSignatureData()); }

}
