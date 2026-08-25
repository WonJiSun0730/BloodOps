package com.shop.changhyeon.management.service;

import com.shop.changhyeon.management.dto.*;
import com.shop.changhyeon.management.entity.DonorConfirmation;
import com.shop.changhyeon.management.repository.DonorConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service @RequiredArgsConstructor
public class DonorConfirmationService {
    private final DonorConfirmationRepository repository;
    @Value("${app.identity-hash-pepper:change-this-in-production}") private String pepper;

    @Transactional(readOnly=true)
    public DonorConfirmation getConfirmation(Long id) { return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 헌혈자 확인 내역이 존재하지 않습니다. ID="+id)); }

    @Transactional
    public DonorIdentityResponseDto verifyIdentity(DonorIdentityRequestDto dto) {
        String digits = dto.residentRegistrationNumber().replaceAll("\\D", "");
        String hash = hash(digits);
        var existing = repository.findFirstByNameAndResidentNumberHash(dto.name().trim(), hash);
        DonorConfirmation donor = existing.orElseGet(() -> {
            DonorConfirmation created = new DonorConfirmation();
            created.setName(dto.name().trim()); created.setBirthDate(toBirthDate(digits));
            created.setResidentNumberHash(hash); created.setResidentNumberMasked(digits.substring(0,6)+"-"+digits.charAt(6)+"******");
            return repository.save(created);
        });
        return new DonorIdentityResponseDto(donor.getConfirmationId(), existing.isPresent(), donor.getResidentNumberMasked(), DonorConfirmationResponseDto.from(donor));
    }

    @Transactional
    public void updateConfirmation(Long id, DonorConfirmationDto d) { getConfirmation(id).updateDonorConfirmation(d.getDonorName(),d.getDonorBirth(),d.getHeight(),d.getWeight(),d.getBloodType(),d.getPhone(),d.getEmail(),d.getAddress(),d.getSignatureData()); }

    private String hash(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((pepper+":"+value).getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException("신분 식별값 암호화에 실패했습니다.",e); } }
    private String toBirthDate(String d) { int yy=Integer.parseInt(d.substring(0,2)); int code=d.charAt(6)-'0'; int century=(code==1||code==2||code==5||code==6)?1900:2000; return "%04d-%s-%s".formatted(century+yy,d.substring(2,4),d.substring(4,6)); }
}
