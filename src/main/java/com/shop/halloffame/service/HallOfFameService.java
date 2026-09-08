package com.shop.halloffame.service;

import com.shop.halloffame.dto.DonationCountProjection;
import com.shop.halloffame.dto.DonationRankingItemResponse;
import com.shop.halloffame.dto.DonationRankingResponse;
import com.shop.halloffame.dto.MyDonationSummaryResponse;
import com.shop.halloffame.repository.HallOfFameRepository;
import com.shop.member.entity.Member;
import com.shop.member.service.MemberService;
import com.shop.reservation.constant.DonationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HallOfFameService {

    private final HallOfFameRepository hallOfFameRepository;
    private final MemberService memberService;


    public List<DonationRankingResponse>
    getAllRankings() {

        return List.of(
                createRanking(
                        "TOTAL",
                        "전체",
                        null
                ),

                createRanking(
                        "WHOLE_BLOOD",
                        "전혈",
                        DonationType.WHOLE_BLOOD
                ),

                createRanking(
                        "PLASMA",
                        "혈장",
                        DonationType.PLASMA
                ),

                createRanking(
                        "PLATELET",
                        "혈소판",
                        DonationType.PLATELET
                ),

                createRanking(
                        "PLATELET_PLASMA",
                        "혈소판혈장",
                        DonationType.PLATELET_PLASMA
                )
        );
    }


    public MyDonationSummaryResponse
    getMyDonationSummary(
            String authenticationIdentifier
    ) {
        Member member =
                memberService
                        .findByAuthenticationIdentifier(
                                authenticationIdentifier
                        );

        if (member == null) {
            throw new IllegalArgumentException(
                    "회원 정보를 찾을 수 없습니다."
            );
        }

        Long memberId =
                member.getId();

        return new MyDonationSummaryResponse(
                member.getName(),
                hallOfFameRepository
                        .countByMember_Id(
                                memberId
                        ),
                hallOfFameRepository
                        .countByMember_IdAndDonationType(
                                memberId,
                                DonationType.WHOLE_BLOOD
                        ),
                hallOfFameRepository
                        .countByMember_IdAndDonationType(
                                memberId,
                                DonationType.PLASMA
                        ),
                hallOfFameRepository
                        .countByMember_IdAndDonationType(
                                memberId,
                                DonationType.PLATELET
                        ),
                hallOfFameRepository
                        .countByMember_IdAndDonationType(
                                memberId,
                                DonationType.PLATELET_PLASMA
                        )
        );
    }


    private DonationRankingResponse
    createRanking(
            String category,
            String categoryLabel,
            DonationType donationType
    ) {
        List<DonationCountProjection> rows =
                donationType == null
                        ? hallOfFameRepository
                        .findOverallRanking(
                                PageRequest.of(
                                        0,
                                        3
                                )
                        )
                        : hallOfFameRepository
                        .findRankingByDonationType(
                                donationType,
                                PageRequest.of(
                                        0,
                                        3
                                )
                        );

        List<DonationRankingItemResponse> rankings =
                rows.stream()
                        .map(
                                row ->
                                        new DonationRankingItemResponse(
                                                rows.indexOf(row)
                                                        + 1,
                                                row.getMemberId(),
                                                maskName(
                                                        row.getMemberName()
                                                ),
                                                row.getDonationCount()
                                        )
                        )
                        .toList();

        return new DonationRankingResponse(
                category,
                categoryLabel,
                rankings
        );
    }


    private String maskName(
            String name
    ) {
        if (
                name == null
                        || name.isBlank()
        ) {
            return "익명";
        }

        String trimmedName =
                name.trim();

        int length =
                trimmedName.length();

        if (length == 1) {
            return trimmedName;
        }

        if (length == 2) {
            return trimmedName.substring(
                    0,
                    1
            ) + "*";
        }

        if (length == 3) {
            return trimmedName.substring(
                    0,
                    1
            )
                    + "*"
                    + trimmedName.substring(
                    2
            );
        }

        return trimmedName.substring(
                0,
                2
        )
                + "*".repeat(
                length - 3
        )
                + trimmedName.substring(
                length - 1
        );
    }
}