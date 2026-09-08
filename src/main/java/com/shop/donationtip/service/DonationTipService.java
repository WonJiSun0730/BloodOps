package com.shop.donationtip.service;

import com.shop.donationtip.constant.DonationTipCategory;
import com.shop.donationtip.dto.DonationTipItemRequest;
import com.shop.donationtip.dto.DonationTipRequest;
import com.shop.donationtip.dto.DonationTipResponse;
import com.shop.donationtip.entity.DonationTip;
import com.shop.donationtip.entity.DonationTipItem;
import com.shop.donationtip.repository.DonationTipRepository;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import com.shop.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonationTipService {

    private final DonationTipRepository donationTipRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;


    public List<DonationTipResponse> getAll(
            DonationTipCategory category
    ) {
        List<DonationTip> tips =
                category == null
                        ? donationTipRepository
                        .findAll()
                        : donationTipRepository
                        .findByCategoryOrderByRegTimeDesc(
                                category
                        );

        return tips.stream()
                .sorted(
                        Comparator.comparing(
                                        DonationTip::getRegTime
                                )
                                .reversed()
                )
                .map(
                        DonationTipResponse::from
                )
                .toList();
    }


    public DonationTipResponse getOne(
            Long tipId
    ) {
        return DonationTipResponse.from(
                getTip(tipId)
        );
    }


    public List<DonationTipResponse> getLatest() {
        return donationTipRepository
                .findTop3ByOrderByRegTimeDesc()
                .stream()
                .map(
                        DonationTipResponse::from
                )
                .toList();
    }


    @Transactional
    public DonationTipResponse create(
            String loginId,
            DonationTipRequest request
    ) {
        validateRequest(
                request
        );

        Member member =
                getMember(
                        loginId
                );

        DonationTip tip =
                new DonationTip(
                        member,
                        request.category(),
                        request.title().trim()
                );

        addItems(
                tip,
                request.items()
        );

        DonationTip saved =
                donationTipRepository.save(
                        tip
                );

        return DonationTipResponse.from(
                saved
        );
    }


    @Transactional
    public DonationTipResponse update(
            Long tipId,
            DonationTipRequest request
    ) {
        validateRequest(
                request
        );

        DonationTip tip =
                getTip(
                        tipId
                );

        tip.update(
                request.category(),
                request.title().trim()
        );

        tip.clearItems();

        addItems(
                tip,
                request.items()
        );

        return DonationTipResponse.from(
                tip
        );
    }


    @Transactional
    public void delete(
            Long tipId
    ) {
        DonationTip tip =
                getTip(
                        tipId
                );

        donationTipRepository.delete(
                tip
        );
    }


    private void addItems(
            DonationTip tip,
            List<DonationTipItemRequest> items
    ) {
        items.stream()
                .sorted(
                        Comparator.comparing(
                                DonationTipItemRequest::itemOrder
                        )
                )
                .forEach(
                        item ->
                                tip.addItem(
                                        new DonationTipItem(
                                                item.itemOrder(),
                                                normalizeOptionalText(
                                                        item.title()
                                                ),
                                                item.content()
                                                        .trim(),
                                                normalizeOptionalText(
                                                        item.imagePath()
                                                )
                                        )
                                )
                );
    }


    private DonationTip getTip(
            Long tipId
    ) {
        return donationTipRepository
                .findById(
                        tipId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "헌혈팁을 찾을 수 없습니다."
                                )
                );
    }


    private Member getMember(
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

        return member;
    }


    private void validateRequest(
            DonationTipRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "헌혈팁 내용을 입력해주세요."
            );
        }

        if (request.category() == null) {
            throw new IllegalArgumentException(
                    "헌혈팁 분류를 선택해주세요."
            );
        }

        if (
                request.title() == null
                        || request.title().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "제목을 입력해주세요."
            );
        }

        if (
                request.title()
                        .trim()
                        .length()
                        > 120
        ) {
            throw new IllegalArgumentException(
                    "제목은 120자 이하로 입력해주세요."
            );
        }

        if (
                request.items() == null
                        || request.items().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "헌혈팁 항목을 1개 이상 등록해주세요."
            );
        }

        if (
                request.items().size()
                        > 12
        ) {
            throw new IllegalArgumentException(
                    "헌혈팁 항목은 최대 12개까지 등록할 수 있습니다."
            );
        }

        for (
                int index = 0;
                index < request.items().size();
                index++
        ) {
            DonationTipItemRequest item =
                    request.items().get(
                            index
                    );

            if (item == null) {
                throw new IllegalArgumentException(
                        "헌혈팁 항목을 확인해주세요."
                );
            }

            if (
                    item.itemOrder() == null
                            || item.itemOrder() < 1
            ) {
                throw new IllegalArgumentException(
                        "헌혈팁 번호를 확인해주세요."
                );
            }

            if (
                    item.content() == null
                            || item.content().isBlank()
            ) {
                throw new IllegalArgumentException(
                        (index + 1)
                                + "번 헌혈팁 내용을 입력해주세요."
                );
            }

            if (
                    item.content()
                            .trim()
                            .length()
                            > 500
            ) {
                throw new IllegalArgumentException(
                        (index + 1)
                                + "번 헌혈팁은 500자 이하로 입력해주세요."
                );
            }

            if (
                    item.title() != null
                            && item.title()
                            .trim()
                            .length()
                            > 120
            ) {
                throw new IllegalArgumentException(
                        (index + 1)
                                + "번 항목 제목은 120자 이하로 입력해주세요."
                );
            }
        }
    }


    private String normalizeOptionalText(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}