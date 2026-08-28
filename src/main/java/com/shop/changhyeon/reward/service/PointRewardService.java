package com.shop.changhyeon.reward.service;

import com.shop.changhyeon.reward.constant.PointTransactionType;
import com.shop.changhyeon.reward.dto.MyPointResponse;
import com.shop.changhyeon.reward.dto.PointTransactionResponse;
import com.shop.changhyeon.reward.entity.PointTransaction;
import com.shop.changhyeon.reward.repository.PointTransactionRepository;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import com.shop.reservation.constant.DonationType;
import com.shop.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointRewardService {

    private final PointTransactionRepository pointTransactionRepository;
    private final MemberRepository memberRepository;

    public void awardDonationPoints(Member member, Reservation reservation, DonationType donationType) {
        if (pointTransactionRepository.existsByReservation_Id(reservation.getId())) {
            throw new IllegalStateException("이미 포인트가 지급된 예약입니다.");
        }

        int points = PointRewardPolicy.pointsFor(donationType);
        pointTransactionRepository.save(
                PointTransaction.builder()
                        .member(member)
                        .reservation(reservation)
                        .transactionType(PointTransactionType.DONATION_REWARD)
                        .donationType(donationType)
                        .points(points)
                        .description(PointRewardPolicy.displayName(donationType) + " 헌혈 완료")
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public MyPointResponse getMyPoints(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        long balance = pointTransactionRepository.sumPointsByMemberId(member.getId());
        var transactions = pointTransactionRepository
                .findByMember_IdOrderByCreatedAtDescIdDesc(member.getId())
                .stream()
                .map(PointTransactionResponse::from)
                .toList();

        return new MyPointResponse(balance, transactions);
    }
}
