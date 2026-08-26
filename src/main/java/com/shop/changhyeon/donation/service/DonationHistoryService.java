package com.shop.changhyeon.donation.service;

import com.shop.center.entity.Center;
import com.shop.changhyeon.donation.dto.DonationHistoryResponseDto;
import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.changhyeon.donation.repository.DonationHistoryRepository;
import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.entity.Reservation;
import com.shop.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class DonationHistoryService {

    private final DonationHistoryRepository donationHistoryRepository;
    private final ReservationRepository reservationRepository;

    public DonationHistoryResponseDto completeDonation(Long reservationId, LocalDate donationDate) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalStateException("취소된 예약은 헌혈 완료 처리할 수 없습니다.");
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("완료 상태인 예약의 헌혈 기록을 찾을 수 없습니다.");
        }

        LocalDate completedDate = donationDate == null ? LocalDate.now() : donationDate;
        if (completedDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("헌혈일자는 미래 날짜로 등록할 수 없습니다.");
        }

        Center center = reservation.getSlot().getCenter();
        DonationHistory history = DonationHistory.builder()
                .donorName(reservation.getMember().getName())
                .donationDate(completedDate)
                .donationType(reservation.getSlot().getDonationType())
                .bloodCenterName(center.getBloodCenterName())
                .donationPlaceName(center.getCenterName())
                .build();

        reservation.setStatus(ReservationStatus.COMPLETED);
        return DonationHistoryResponseDto.from(donationHistoryRepository.save(history));
    }
}
