package com.shop.bloodhold.service;

import com.shop.bloodhold.constant.BloodHoldContactType;
import com.shop.bloodhold.dto.BloodHoldGuideResponse;
import com.shop.bloodhold.entity.BloodHoldContact;
import com.shop.bloodhold.repository.BloodHoldContactRepository;
import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.changhyeon.donation.repository.DonationHistoryRepository;
import com.shop.member.entity.Member;
import com.shop.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BloodHoldGuideService {

    private final DonationHistoryRepository donationHistoryRepository;
    private final BloodHoldContactRepository bloodHoldContactRepository;
    private final CenterRepository centerRepository;
    private final MemberService memberService;


    public BloodHoldGuideResponse getMyGuide(
            String authenticationIdentifier
    ) {

        Member member =
                memberService.findByAuthenticationIdentifier(
                        authenticationIdentifier
                );


        DonationHistory latestHistory =
                donationHistoryRepository
                        .findByMember_IdOrderByDonationDateDescIdDesc(
                                member.getId()
                        )
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (latestHistory == null) {
            return null;
        }


        BloodHoldContact bloodCenter =
                findBloodCenter(
                        latestHistory.getBloodCenterName()
                );


        Center donationCenter =
                findDonationCenter(
                        latestHistory.getDonationPlaceName()
                );


        BloodHoldGuideResponse.ContactTarget primaryContact =
                new BloodHoldGuideResponse.ContactTarget(
                        "PRIMARY",
                        bloodCenter.getName(),
                        bloodCenter.getAddress(),
                        bloodCenter.getPhone(),
                        bloodCenter.getLatitude(),
                        bloodCenter.getLongitude()
                );


        BloodHoldGuideResponse.ContactTarget secondaryContact =
                new BloodHoldGuideResponse.ContactTarget(
                        "SECONDARY",
                        donationCenter.getCenterName(),
                        donationCenter.getAddress(),
                        donationCenter.getPhone(),
                        donationCenter.getLatitude(),
                        donationCenter.getLongitude()
                );


        return new BloodHoldGuideResponse(
                latestHistory.getDonationDate(),
                latestHistory.getDonationType().name(),
                primaryContact,
                secondaryContact,
                """
                혈액사용 보류 요청은 헌혈증서에 표기된 혈액원으로 연락하는 것이 우선입니다.
                현재 전염성이 높은 질병이 의심되거나 발병한 상태라면 직접 방문하기 전에 먼저 전화로 문의해주세요.
                """
        );
    }

    public List<BloodHoldGuideResponse.ContactTarget>
    getBloodCenters() {

        return bloodHoldContactRepository
                .findByTypeAndActiveTrueOrderByNameAsc(
                        BloodHoldContactType.BLOOD_CENTER
                )
                .stream()
                .map(
                        contact ->
                                new BloodHoldGuideResponse.ContactTarget(
                                        "BLOOD_CENTER",
                                        contact.getName(),
                                        contact.getAddress(),
                                        contact.getPhone(),
                                        contact.getLatitude(),
                                        contact.getLongitude()
                                )
                )
                .toList();
    }

    private BloodHoldContact findBloodCenter(
            String bloodCenterName
    ) {

        String normalizedHistoryName =
                normalizeBloodCenterName(
                        bloodCenterName
                );

        return bloodHoldContactRepository
                .findAll()
                .stream()
                .filter(BloodHoldContact::isActive)
                .filter(
                        contact ->
                                normalizeBloodCenterName(
                                        contact.getName()
                                ).equalsIgnoreCase(
                                        normalizedHistoryName
                                )
                )
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "해당 혈액원의 연락처 정보를 찾을 수 없습니다: "
                                        + bloodCenterName
                        )
                );
    }

    private String normalizeBloodCenterName(
            String bloodCenterName
    ) {

        if (bloodCenterName == null) {
            return "";
        }

        return bloodCenterName
                .trim()
                .replaceAll("\\s+", "")
                .replace("혈액원", "");
    }


    private Center findDonationCenter(
            String donationPlaceName
    ) {

        List<Center> candidates =
                centerRepository
                        .findByCenterNameContainingIgnoreCase(
                                donationPlaceName
                        );


        return candidates.stream()
                .filter(Center::isActive)
                .filter(
                        center ->
                                center.getCenterName()
                                        .equalsIgnoreCase(
                                                donationPlaceName
                                        )
                )
                .findFirst()
                .orElseGet(
                        () -> candidates.stream()
                                .filter(Center::isActive)
                                .findFirst()
                                .orElseThrow(
                                        () -> new IllegalStateException(
                                                "헌혈한 센터 정보를 찾을 수 없습니다: "
                                                        + donationPlaceName
                                        )
                                )
                );
    }
}