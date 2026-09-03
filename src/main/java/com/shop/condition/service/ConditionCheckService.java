package com.shop.condition.service;

import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.changhyeon.donation.repository.DonationHistoryRepository;
import com.shop.changhyeon.reward.constant.PointTransactionType;
import com.shop.changhyeon.reward.entity.PointTransaction;
import com.shop.changhyeon.reward.repository.PointTransactionRepository;
import com.shop.condition.dto.ConditionCheckRequestDto;
import com.shop.condition.dto.ConditionCheckResponseDto;
import com.shop.condition.entity.ConditionCheck;
import com.shop.condition.entity.ConditionCheckReward;
import com.shop.condition.repository.ConditionCheckRepository;
import com.shop.condition.repository.ConditionCheckRewardRepository;
import com.shop.member.entity.Member;
import com.shop.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ConditionCheckService {

    private static final int CONDITION_CHECK_REWARD_POINTS = 30;

    private final DonationHistoryRepository donationHistoryRepository;
    private final ConditionCheckRepository conditionCheckRepository;
    private final ConditionCheckRewardRepository conditionCheckRewardRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final MemberService memberService;

    public ConditionCheckResponseDto createCheck(
            Long donationHistoryId,
            String authenticationIdentifier,
            ConditionCheckRequestDto request
    ) {
        DonationHistory history =
                getOwnedDonationHistory(
                        donationHistoryId,
                        authenticationIdentifier
                );

        ConditionCheck check = conditionCheckRepository.save(
                ConditionCheck.builder()
                        .donationHistory(history)
                        .overallCondition(request.getOverallCondition())
                        .dizziness(request.isDizziness())
                        .nausea(request.isNausea())
                        .bleedingOrBruising(request.isBleedingOrBruising())
                        .armPain(request.isArmPain())
                        .fainting(request.isFainting())
                        .otherSymptom(normalizeOtherSymptom(request.getOtherSymptom()))
                        .build()
        );

        boolean rewardGranted = grantFirstCheckReward(history);

        return ConditionCheckResponseDto.builder()
                .id(check.getId())
                .donationHistoryId(history.getId())
                .overallCondition(check.getOverallCondition())
                .dizziness(check.isDizziness())
                .nausea(check.isNausea())
                .bleedingOrBruising(check.isBleedingOrBruising())
                .armPain(check.isArmPain())
                .fainting(check.isFainting())
                .otherSymptom(check.getOtherSymptom())
                .guidance(createGuidance(check))
                .rewardGranted(rewardGranted)
                .rewardPoints(rewardGranted ? CONDITION_CHECK_REWARD_POINTS : 0)
                .createdAt(check.getCreatedAt())
                .build();
    }

    private boolean grantFirstCheckReward(DonationHistory history) {
        if (conditionCheckRewardRepository
                .existsByDonationHistory_Id(history.getId())) {
            return false;
        }

        PointTransaction transaction = pointTransactionRepository.save(
                PointTransaction.builder()
                        .member(history.getMember())
                        .transactionType(PointTransactionType.CONDITION_CHECK_REWARD)
                        .points(CONDITION_CHECK_REWARD_POINTS)
                        .description("헌혈 후 컨디션 체크 참여")
                        .build()
        );

        conditionCheckRewardRepository.save(
                ConditionCheckReward.builder()
                        .donationHistory(history)
                        .pointTransaction(transaction)
                        .build()
        );

        return true;
    }

    private String normalizeOtherSymptom(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private List<String> createGuidance(ConditionCheck check) {
        List<String> guidance = new ArrayList<>();

        if (check.isFainting()) {
            guidance.add("앉거나 누워서 안정을 취하고, 주변 사람에게 도움을 요청해주세요.");
        }

        if (check.isDizziness() || check.isNausea()) {
            guidance.add("충분히 휴식하고 물이나 음료를 천천히 섭취해주세요.");
        }

        if (check.isBleedingOrBruising()) {
            guidance.add("헌혈 부위를 충분히 압박하고 출혈이 계속되면 의료기관에 문의해주세요.");
        }

        if (check.isArmPain()) {
            guidance.add("헌혈한 팔에 무리가 가지 않도록 하고 통증이 심해지면 의료기관에 문의해주세요.");
        }

        if (guidance.isEmpty()) {
            guidance.add("오늘은 충분히 수분을 섭취하고 무리한 활동은 피해주세요.");
        }

        return guidance;
    }

    public DonationHistory verifyAccess(
            Long donationHistoryId,
            String authenticationIdentifier
    ) {
        return getOwnedDonationHistory(
                donationHistoryId,
                authenticationIdentifier
        );
    }

    private DonationHistory getOwnedDonationHistory(
            Long donationHistoryId,
            String authenticationIdentifier
    ) {
        DonationHistory history = donationHistoryRepository.findById(donationHistoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "헌혈 기록을 찾을 수 없습니다."
                ));

        Member currentMember;

        try {
            currentMember = memberService.findByAuthenticationIdentifier(
                    authenticationIdentifier
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인 회원 정보를 확인할 수 없습니다."
            );
        }

        if (!history.getMember().getId().equals(currentMember.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "본인의 헌혈 기록만 확인할 수 있습니다."
            );
        }

        return history;
    }
}