package com.shop.reservation.service;

import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.dto.AdminReservationResponseDto;
import com.shop.reservation.entity.Reservation;
import com.shop.reservation.entity.ReservationSlot;
import com.shop.reservation.repository.ReservationRepository;
import com.shop.reservation.repository.ReservationSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository
            reservationRepository;

    private final ReservationSlotRepository
            reservationSlotRepository;

    private final MemberRepository
            memberRepository;

    public AdminReservationResponseDto createReservation(
            Long slotId,
            String email
    ) {
        if (slotId == null) {
            throw new IllegalArgumentException(
                    "예약 슬롯을 선택해주세요."
            );
        }

        Member member =
                memberRepository.findByEmail(email);

        if (member == null) {
            throw new IllegalArgumentException(
                    "회원 정보를 찾을 수 없습니다."
            );
        }

        ReservationSlot slot =
                reservationSlotRepository.findById(slotId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "예약 슬롯을 찾을 수 없습니다."
                                )
                        );

        if (!slot.isActive()) {
            throw new IllegalStateException(
                    "현재 예약할 수 없는 시간입니다."
            );
        }

        long reservedCount =
                reservationRepository
                        .countBySlot_IdAndStatus(
                                slotId,
                                ReservationStatus.RESERVED
                        );

        if (reservedCount >= slot.getCapacity()) {
            throw new IllegalStateException(
                    "예약 정원이 마감되었습니다."
            );
        }

        boolean alreadyReserved =
                reservationRepository
                        .existsByMember_IdAndSlot_IdAndStatus(
                                member.getId(),
                                slotId,
                                ReservationStatus.RESERVED
                        );

        if (alreadyReserved) {
            throw new IllegalStateException(
                    "이미 해당 시간에 예약되어 있습니다."
            );
        }

        Reservation reservation =
                Reservation.builder()
                        .member(member)
                        .slot(slot)
                        .status(
                                ReservationStatus.RESERVED
                        )
                        .build();

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return new AdminReservationResponseDto(
                savedReservation
        );
    }

    /*
     * ADMIN 예약 취소
     */
    public AdminReservationResponseDto cancelReservation(
            Long reservationId
    ) {
        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "예약을 찾을 수 없습니다."
                                )
                        );

        if (
                reservation.getStatus()
                        == ReservationStatus.CANCELED
        ) {
            throw new IllegalStateException(
                    "이미 취소된 예약입니다."
            );
        }

        reservation.setStatus(
                ReservationStatus.CANCELED
        );

        return new AdminReservationResponseDto(
                reservation
        );
    }

    /*
     * ADMIN 예약 관리 화면의 전체 예약 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<AdminReservationResponseDto>
    getAdminReservations() {

        return reservationRepository
                .findAllByOrderByIdDesc()
                .stream()
                .map(AdminReservationResponseDto::new)
                .toList();
    }
}