package com.shop.changhyeon.management.service;

import com.shop.changhyeon.management.dto.DonorConfirmationDto;
import com.shop.changhyeon.management.entity.DonorConfirmation;
import com.shop.changhyeon.management.repository.DonorConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DonorConfirmationService {

    private final DonorConfirmationRepository confirmationRepository;

    @Transactional(readOnly = true)
    public DonorConfirmation getConfirmation(Long confirmationId) {
        return confirmationRepository.findById(confirmationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 헌혈자 확인 내역이 존재하지 않습니다. ID=" + confirmationId));
    }

    @Transactional
    public void updateConfirmation(Long confirmationId, DonorConfirmationDto dto) {
        DonorConfirmation confirmation = getConfirmation(confirmationId);
        confirmation.updateDonorConfirmation(
                dto.getDonorName(),
                dto.getDonorBirth(),
                dto.getHeight(),
                dto.getWeight(),
                dto.getBloodType(),
                dto.getPhone(),
                dto.getEmail(),
                dto.getAddress(),
                dto.getSignatureData()
        );
    }
}
