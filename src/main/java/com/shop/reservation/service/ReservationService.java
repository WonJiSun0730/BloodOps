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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
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

        LocalDate today =
                LocalDate.now();

        if (
                slot.getReservationDate()
                        .isBefore(today)
        ) {
            throw new IllegalStateException(
                    "이미 지난 날짜에는 예약할 수 없습니다."
            );
        }

        if (
                slot.getReservationDate()
                        .isEqual(today)
                        && !slot.getStartTime()
                        .isAfter(LocalTime.now())
        ) {
            throw new IllegalStateException(
                    "이미 지난 시간에는 예약할 수 없습니다."
            );
        }

        if (!slot.isActive()) {
            throw new IllegalStateException(
                    "현재 예약할 수 없는 시간입니다."
            );
        }

        boolean alreadyReservedOnDate =
                reservationRepository
                        .existsByMember_IdAndSlot_ReservationDateAndStatus(
                                member.getId(),
                                slot.getReservationDate(),
                                ReservationStatus.RESERVED
                        );

        if (alreadyReservedOnDate) {
            throw new IllegalStateException(
                    "같은 날짜에는 하나의 헌혈 예약만 할 수 있습니다."
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
     * 로그인한 회원의 예약 내역 조회
     */
    @Transactional(readOnly = true)
    public List<AdminReservationResponseDto> getMyReservations(
            String email
    ) {
        Member member =
                memberRepository.findByEmail(email);

        if (member == null) {
            throw new IllegalArgumentException(
                    "회원 정보를 찾을 수 없습니다."
            );
        }

        return reservationRepository
                .findByMember_EmailOrderByIdDesc(email)
                .stream()
                .map(AdminReservationResponseDto::new)
                .toList();
    }

    /*
     * 로그인한 회원 본인의 예약 슬롯 변경
     */
    public AdminReservationResponseDto changeMyReservationSlot(
            Long reservationId,
            Long newSlotId,
            String email
    ) {
        if (newSlotId == null) {
            throw new IllegalArgumentException(
                    "변경할 예약 시간을 선택해주세요."
            );
        }

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "예약을 찾을 수 없습니다."
                                )
                        );

        if (
                !reservation.getMember()
                        .getEmail()
                        .equals(email)
        ) {
            throw new IllegalStateException(
                    "본인의 예약만 변경할 수 있습니다."
            );
        }

        if (
                reservation.getStatus()
                        == ReservationStatus.CANCELED
        ) {
            throw new IllegalStateException(
                    "취소된 예약은 변경할 수 없습니다."
            );
        }

        if (
                reservation.getSlot()
                        .getId()
                        .equals(newSlotId)
        ) {
            throw new IllegalStateException(
                    "현재 예약된 시간과 동일합니다."
            );
        }

        ReservationSlot newSlot =
                reservationSlotRepository
                        .findById(newSlotId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "변경할 예약 시간을 찾을 수 없습니다."
                                )
                        );

        if (!newSlot.isActive()) {
            throw new IllegalStateException(
                    "현재 예약할 수 없는 시간입니다."
            );
        }

        /*
         * 사용자 변경은 기존 예약과
         * 같은 센터 / 날짜 / 헌혈 종류 안에서만 허용합니다.
         */
        ReservationSlot oldSlot =
                reservation.getSlot();

        if (
                !oldSlot.getCenter()
                        .getId()
                        .equals(
                                newSlot.getCenter().getId()
                        )
                        || !oldSlot.getReservationDate()
                        .equals(
                                newSlot.getReservationDate()
                        )
                        || oldSlot.getDonationType()
                        != newSlot.getDonationType()
        ) {
            throw new IllegalStateException(
                    "같은 센터와 날짜, 헌혈 종류의 예약 시간만 변경할 수 있습니다."
            );
        }

        long reservedCount =
                reservationRepository
                        .countBySlot_IdAndStatus(
                                newSlotId,
                                ReservationStatus.RESERVED
                        );

        if (
                reservedCount
                        >= newSlot.getCapacity()
        ) {
            throw new IllegalStateException(
                    "변경할 시간의 예약 정원이 마감되었습니다."
            );
        }

        reservation.setSlot(
                newSlot
        );

        return new AdminReservationResponseDto(
                reservation
        );
    }

    /*
     * ADMIN 예약 슬롯 변경
     */
    public AdminReservationResponseDto changeReservationSlot(
            Long reservationId,
            Long newSlotId
    ) {
        if (newSlotId == null) {
            throw new IllegalArgumentException(
                    "변경할 예약 슬롯을 선택해주세요."
            );
        }

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "예약을 찾을 수 없습니다."
                                )
                        );

        /*
         * 취소된 예약은 다시 슬롯을 배정하지 않습니다.
         */
        if (
                reservation.getStatus()
                        == ReservationStatus.CANCELED
        ) {
            throw new IllegalStateException(
                    "취소된 예약은 변경할 수 없습니다."
            );
        }

        ReservationSlot oldSlot =
                reservation.getSlot();

        /*
         * 같은 슬롯으로의 변경 요청 차단
         */
        if (
                oldSlot.getId()
                        .equals(newSlotId)
        ) {
            throw new IllegalStateException(
                    "현재 예약된 슬롯과 동일합니다."
            );
        }

        ReservationSlot newSlot =
                reservationSlotRepository
                        .findById(newSlotId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "변경할 예약 슬롯을 찾을 수 없습니다."
                                )
                        );

        /*
         * 관리자가 중지한 슬롯에는
         * 예약을 이동시키지 않습니다.
         */
        if (!newSlot.isActive()) {
            throw new IllegalStateException(
                    "현재 예약할 수 없는 시간입니다."
            );
        }

        /*
         * 신규 슬롯 정원 확인
         */
        long reservedCount =
                reservationRepository
                        .countBySlot_IdAndStatus(
                                newSlotId,
                                ReservationStatus.RESERVED
                        );

        if (
                reservedCount
                        >= newSlot.getCapacity()
        ) {
            throw new IllegalStateException(
                    "변경할 슬롯의 예약 정원이 마감되었습니다."
            );
        }

        /*
         * 같은 회원이 신규 슬롯에 이미
         * 별도 예약을 가지고 있는지 확인합니다.
         */
        boolean alreadyReserved =
                reservationRepository
                        .existsByMember_IdAndSlot_IdAndStatus(
                                reservation.getMember().getId(),
                                newSlotId,
                                ReservationStatus.RESERVED
                        );

        if (alreadyReserved) {
            throw new IllegalStateException(
                    "해당 회원은 이미 변경할 시간에 예약되어 있습니다."
            );
        }

        /*
         * 실제 슬롯 변경.
         *
         * Reservation의 slot_id가 변경되므로
         * 기존 슬롯 RESERVED 집계에서는 자동으로 빠지고
         * 신규 슬롯 RESERVED 집계에는 자동으로 포함됩니다.
         */
        reservation.setSlot(newSlot);

        return new AdminReservationResponseDto(
                reservation
        );
    }

    /*
     * 로그인한 회원 본인의 예약 취소
     */
    public AdminReservationResponseDto cancelMyReservation(
            Long reservationId,
            String email
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
                !reservation.getMember()
                        .getEmail()
                        .equals(email)
        ) {
            throw new IllegalStateException(
                    "본인의 예약만 취소할 수 있습니다."
            );
        }

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
     * ADMIN 예약 상세 조회
     */
    @Transactional(readOnly = true)
    public AdminReservationResponseDto getAdminReservation(
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

        return new AdminReservationResponseDto(
                reservation
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminReservationResponseDto>
    getAdminReservations(
            String keyword,
            ReservationStatus status,
            Pageable pageable
    ) {

        String normalizedKeyword =
                keyword == null
                        ? ""
                        : keyword.trim();

        return reservationRepository
                .findAdminReservations(
                        normalizedKeyword,
                        status,
                        pageable
                )
                .map(AdminReservationResponseDto::new);
    }
}