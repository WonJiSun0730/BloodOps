package com.shop.changhyeon.donation.service;

import com.shop.center.entity.Center;
import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.changhyeon.donation.repository.DonationHistoryRepository;
import com.shop.changhyeon.reward.service.PointRewardService;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import com.shop.member.service.MemberService;
import com.shop.reservation.constant.DonationType;
import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.entity.Reservation;
import com.shop.reservation.entity.ReservationSlot;
import com.shop.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationHistoryServiceTest {

    @Mock DonationHistoryRepository donationHistoryRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock MemberRepository memberRepository;
    @Mock PointRewardService pointRewardService;
    @Mock MemberService memberService;
    @InjectMocks DonationHistoryService donationHistoryService;

    @Test
    void 완료처리하면_헌혈기록을_저장하고_예약상태를_변경한다() {
        Center center = Center.builder()
                .bloodCenterName("인천")
                .centerName("헌혈의집 구월센터")
                .address("인천광역시")
                .build();
        Member member = new Member();
        member.setName("홍길동");
        ReservationSlot slot = ReservationSlot.builder()
                .center(center)
                .donationType(DonationType.PLATELET)
                .build();
        Reservation reservation = Reservation.builder()
                .id(1L)
                .member(member)
                .slot(slot)
                .status(ReservationStatus.RESERVED)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(donationHistoryRepository.save(any(DonationHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        donationHistoryService.completeDonation(1L, LocalDate.of(2026, 8, 23));

        ArgumentCaptor<DonationHistory> captor = ArgumentCaptor.forClass(DonationHistory.class);
        verify(donationHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getMember()).isSameAs(member);
        assertThat(captor.getValue().getReservation()).isSameAs(reservation);
        assertThat(captor.getValue().getDonorName()).isEqualTo("홍길동");
        assertThat(captor.getValue().getDonationDate()).isEqualTo(LocalDate.of(2026, 8, 23));
        assertThat(captor.getValue().getDonationType()).isEqualTo(DonationType.PLATELET);
        assertThat(captor.getValue().getBloodCenterName()).isEqualTo("인천");
        assertThat(captor.getValue().getDonationPlaceName()).isEqualTo("헌혈의집 구월센터");
        verify(pointRewardService).awardDonationPoints(
                member,
                reservation,
                DonationType.PLATELET
        );
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    void 취소된_예약은_완료처리할_수_없다() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.CANCELED)
                .build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> donationHistoryService.completeDonation(1L, LocalDate.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("취소된 예약은 헌혈 완료 처리할 수 없습니다.");
        verify(donationHistoryRepository, never()).save(any());
    }

    @Test
    void 로그인한_회원의_PK로_헌혈기록을_최신순_조회한다() {
        Member member = new Member();
        member.setId(7L);
        when(memberService.findByAuthenticationIdentifier("user@bloodops.local"))
                .thenReturn(member);
        when(donationHistoryRepository.findByMember_IdOrderByDonationDateDescIdDesc(7L))
                .thenReturn(List.of());

        donationHistoryService.getMyDonationHistory("user@bloodops.local");

        verify(donationHistoryRepository)
                .findByMember_IdOrderByDonationDateDescIdDesc(7L);
    }

    @Test
    void 의료진은_전체_헌혈기록을_최신순_조회한다() {
        when(donationHistoryRepository.findMedicalDonationHistory(null))
                .thenReturn(List.of());

        donationHistoryService.getAllDonationHistory();

        verify(donationHistoryRepository)
                .findMedicalDonationHistory(null);
    }

    @Test
    void 의료진은_선택한_센터의_헌혈기록만_조회한다() {
        when(donationHistoryRepository.findMedicalDonationHistory(15L))
                .thenReturn(List.of());

        donationHistoryService.getAllDonationHistory(15L);

        verify(donationHistoryRepository)
                .findMedicalDonationHistory(15L);
    }
}
