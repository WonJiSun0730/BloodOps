package com.shop.changhyeon.donation.service;

import com.shop.center.entity.Center;
import com.shop.changhyeon.donation.dto.DonationEligibilityResponseDto;
import com.shop.changhyeon.donation.dto.DonationHistoryResponseDto;
import com.shop.changhyeon.donation.dto.MedicalDonationHistoryResponseDto;
import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.changhyeon.donation.repository.DonationHistoryRepository;
import com.shop.changhyeon.reward.service.PointRewardService;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import com.shop.member.service.MemberService;
import com.shop.reservation.constant.DonationType;
import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.entity.Reservation;
import com.shop.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DonationHistoryService {

    private final DonationHistoryRepository donationHistoryRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final PointRewardService pointRewardService;
    private final MemberService memberService;

    public DonationHistoryResponseDto completeDonation(Long reservationId, LocalDate donationDate) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalStateException("취소된 예약은 헌혈 완료 처리할 수 없습니다.");
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("완료 상태인 예약의 헌혈 기록을 찾을 수 없습니다.");
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate completedDate = donationDate == null ? today : donationDate;
        if (completedDate.isAfter(today)) {
            throw new IllegalArgumentException("헌혈일자는 미래 날짜로 등록할 수 없습니다.");
        }

        Center center = reservation.getSlot().getCenter();
        DonationHistory history = DonationHistory.builder()
                .member(reservation.getMember())
                .reservation(reservation)
                .donorName(reservation.getMember().getName())
                .donationDate(completedDate)
                .donationType(reservation.getSlot().getDonationType())
                .bloodCenterName(center.getBloodCenterName())
                .donationPlaceName(center.getCenterName())
                .build();

        DonationHistory savedHistory = donationHistoryRepository.save(history);
        pointRewardService.awardDonationPoints(
                reservation.getMember(),
                reservation,
                reservation.getSlot().getDonationType()
        );
        reservation.setStatus(ReservationStatus.COMPLETED);
        return DonationHistoryResponseDto.from(savedHistory);
    }

    @Transactional(readOnly = true)
    public List<DonationHistoryResponseDto> getMyDonationHistory(String email) {
        Member member =
                memberService.findByAuthenticationIdentifier(
                        email
                );

        return donationHistoryRepository
                .findByMember_IdOrderByDonationDateDescIdDesc(member.getId())
                .stream()
                .map(DonationHistoryResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicalDonationHistoryResponseDto> getAllDonationHistory() {
        return getAllDonationHistory(null);
    }

    @Transactional(readOnly = true)
    public List<MedicalDonationHistoryResponseDto> getAllDonationHistory(
            Long centerId
    ) {
        List<DonationHistory> histories =
                donationHistoryRepository.findMedicalDonationHistory(
                        centerId
                );

        return histories.stream()
                .map(MedicalDonationHistoryResponseDto::from)
                .toList();
    }

    public DonationEligibilityResponseDto getMyDonationEligibility(
            String email
    ) {
        Member member =
                memberService.findByAuthenticationIdentifier(
                        email
                );

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate oneYearAgo = today.minusYears(1);

        List<DonationHistory> histories =
                donationHistoryRepository
                        .findByMember_IdOrderByDonationDateDescIdDesc(
                                member.getId()
                        );


        /*
         * 마지막 헌혈일 기준 간격 제한
         *
         * 전혈: 8주
         * 성분헌혈: 2주
         */
        LocalDate lastDonationDate =
                histories.stream()
                        .map(
                                DonationHistory::getDonationDate
                        )
                        .max(
                                LocalDate::compareTo
                        )
                        .orElse(null);

        LocalDate wholeBloodIntervalDate =
                lastDonationDate == null
                        ? today
                        : lastDonationDate.plusWeeks(8);

        LocalDate componentIntervalDate =
                lastDonationDate == null
                        ? today
                        : lastDonationDate.plusWeeks(2);


        /*
         * 최근 1년 기록
         */
        List<DonationHistory> lastYearHistories =
                histories.stream()
                        .filter(
                                history ->
                                        !history
                                                .getDonationDate()
                                                .isBefore(
                                                        oneYearAgo
                                                )
                        )
                        .toList();


        /*
         * 전혈:
         * 최근 1년 최대 5회
         */
        List<DonationHistory> wholeBloodHistories =
                lastYearHistories.stream()
                        .filter(
                                history ->
                                        history
                                                .getDonationType()
                                                == DonationType.WHOLE_BLOOD
                        )
                        .sorted(
                                (first, second) ->
                                        first
                                                .getDonationDate()
                                                .compareTo(
                                                        second
                                                                .getDonationDate()
                                                )
                        )
                        .toList();

        int wholeBloodCount =
                wholeBloodHistories.size();

        LocalDate wholeBloodCountReleaseDate =
                today;

        if (wholeBloodCount >= 5) {
            DonationHistory oldestCountedHistory =
                    wholeBloodHistories.get(
                            wholeBloodCount - 5
                    );

            wholeBloodCountReleaseDate =
                    oldestCountedHistory
                            .getDonationDate()
                            .plusYears(1)
                            .plusDays(1);
        }


        /*
         * 혈소판 + 혈소판혈장:
         * 최근 1년 최대 24회
         *
         * 혈장 단독은 횟수 제한 없음.
         */
        List<DonationHistory> plateletHistories =
                lastYearHistories.stream()
                        .filter(
                                history ->
                                        history
                                                .getDonationType()
                                                == DonationType.PLATELET
                                                || history
                                                .getDonationType()
                                                == DonationType.PLATELET_PLASMA
                        )
                        .sorted(
                                (first, second) ->
                                        first
                                                .getDonationDate()
                                                .compareTo(
                                                        second
                                                                .getDonationDate()
                                                )
                        )
                        .toList();

        int plateletCount =
                plateletHistories.size();

        LocalDate plateletCountReleaseDate =
                today;

        if (plateletCount >= 24) {
            DonationHistory oldestCountedHistory =
                    plateletHistories.get(
                            plateletCount - 24
                    );

            plateletCountReleaseDate =
                    oldestCountedHistory
                            .getDonationDate()
                            .plusYears(1)
                            .plusDays(1);
        }


        /*
         * 최종 가능일
         */
        LocalDate wholeBloodNextAvailableDate =
                laterDate(
                        wholeBloodIntervalDate,
                        wholeBloodCountReleaseDate
                );

        LocalDate plateletNextAvailableDate =
                laterDate(
                        componentIntervalDate,
                        plateletCountReleaseDate
                );

        LocalDate plasmaNextAvailableDate =
                componentIntervalDate;

        LocalDate plateletPlasmaNextAvailableDate =
                laterDate(
                        componentIntervalDate,
                        plateletCountReleaseDate
                );

        String wholeBloodRestrictionReason =
                determineRestrictionReason(
                        today,
                        wholeBloodIntervalDate,
                        wholeBloodCountReleaseDate
                );

        String plateletRestrictionReason =
                determineRestrictionReason(
                        today,
                        componentIntervalDate,
                        plateletCountReleaseDate
                );

        String plasmaRestrictionReason =
                componentIntervalDate.isAfter(today)
                        ? "INTERVAL"
                        : "NONE";

        String plateletPlasmaRestrictionReason =
                determineRestrictionReason(
                        today,
                        componentIntervalDate,
                        plateletCountReleaseDate
                );


        return new DonationEligibilityResponseDto(
                wholeBloodNextAvailableDate,
                plateletNextAvailableDate,
                plasmaNextAvailableDate,
                plateletPlasmaNextAvailableDate,

                wholeBloodRestrictionReason,
                plateletRestrictionReason,
                plasmaRestrictionReason,
                plateletPlasmaRestrictionReason,

                wholeBloodCount,
                5,
                plateletCount,
                24
        );
    }

    private String determineRestrictionReason(
            LocalDate today,
            LocalDate intervalDate,
            LocalDate annualLimitReleaseDate
    ) {
        boolean intervalRestricted =
                intervalDate != null
                        && intervalDate.isAfter(today);

        boolean annualRestricted =
                annualLimitReleaseDate != null
                        && annualLimitReleaseDate.isAfter(today);

        if (!intervalRestricted && !annualRestricted) {
            return "NONE";
        }

        if (
                annualRestricted
                        && (
                        !intervalRestricted
                                || !annualLimitReleaseDate.isBefore(intervalDate)
                )
        ) {
            return "ANNUAL_LIMIT";
        }

        return "INTERVAL";
    }

    private LocalDate laterDate(
            LocalDate first,
            LocalDate second
    ) {
        return first.isAfter(second)
                ? first
                : second;
    }
}
