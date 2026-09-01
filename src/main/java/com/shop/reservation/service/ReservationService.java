package com.shop.reservation.service;

import com.shop.changhyeon.donation.dto.DonationEligibilityResponseDto;
import com.shop.changhyeon.donation.service.DonationHistoryService;
import com.shop.changhyeon.identity.ResidentNumberHashService;
import com.shop.member.entity.Member;
import com.shop.member.service.MemberService;
import com.shop.reservation.constant.ReservationStatus;
import com.shop.reservation.dto.AdminReservationResponseDto;
import com.shop.reservation.dto.ReservationCreateDto;
import com.shop.reservation.entity.EquipmentGroup;
import com.shop.reservation.entity.Reservation;
import com.shop.reservation.entity.ReservationSlot;
import com.shop.reservation.exception.ActiveReservationExistsException;
import com.shop.reservation.repository.EquipmentGroupRepository;
import com.shop.reservation.repository.ReservationRepository;
import com.shop.reservation.repository.ReservationSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private final EquipmentGroupRepository
            equipmentGroupRepository;

    private final MemberService
            memberService;

    private final ResidentNumberHashService
            residentNumberHashService;

    private final DonationHistoryService donationHistoryService;

    private final ReservationTimePolicy reservationTimePolicy;

    public AdminReservationResponseDto createReservation(
            ReservationCreateDto request,
            String email
    ) {
        if (request == null || request.getSlotId() == null) {
            throw new IllegalArgumentException(
                    "예약 슬롯을 선택해주세요."
            );
        }

        Long slotId =
                request.getSlotId();

        validateReservationPersonalInfo(
                request
        );

        Member member =
                memberService.findByAuthenticationIdentifier(
                        email
                );

        cancelExpiredReservations(
                member
        );

        ReservationSlot slot =
                reservationSlotRepository.findById(slotId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "예약 슬롯을 찾을 수 없습니다."
                                )
                        );

        if (
                !reservationTimePolicy
                        .isReservableStartTime(
                                slot.getCenter(),
                                slot.getReservationDate(),
                                slot.getDonationType(),
                                slot.getStartTime()
                        )
        ) {
            throw new IllegalStateException(
                    "선택한 시간은 해당 헌혈 종류의 접수 마감시간 이후입니다."
            );
        }

        validateDonationEligibility(
                slot,
                member.getEmail()
        );

        LocalDate today =
                LocalDate.now();

        if (
                !slot.getReservationDate()
                        .isAfter(today)
        ) {
            throw new IllegalStateException(
                    "당일 및 지난 날짜에는 예약할 수 없습니다."
            );
        }

        LocalDate maxReservationDate =
                today.plusMonths(2);

        if (
                slot.getReservationDate()
                        .isAfter(maxReservationDate)
        ) {
            throw new IllegalStateException(
                    "예약은 오늘부터 2개월 이내의 날짜만 가능합니다."
            );
        }

        if (!slot.isActive()) {
            throw new IllegalStateException(
                    "현재 예약할 수 없는 시간입니다."
            );
        }

        /*
         * 한 회원은 동시에 하나의 진행 중 예약만 유지합니다.
         */
        List<Reservation> activeReservations =
                reservationRepository
                        .findByMember_IdAndStatusOrderByIdDesc(
                                member.getId(),
                                ReservationStatus.RESERVED
                        );

        if (!activeReservations.isEmpty()) {

            Reservation existingReservation =
                    activeReservations.get(0);

            if (!request.isReplaceExistingReservation()) {
                throw new ActiveReservationExistsException(
                        new AdminReservationResponseDto(
                                existingReservation
                        )
                );
            }

            /*
             * 과거 정책에서 RESERVED가 여러 건 남아 있을 수도 있으므로
             * 교체 동의 시 진행 중 예약을 모두 취소합니다.
             */
            activeReservations.forEach(
                    activeReservation ->
                            activeReservation.setStatus(
                                    ReservationStatus.CANCELED
                            )
            );
        }

        validateEquipmentCapacity(
                slot,
                null
        );

        LocalDateTime consentedAt =
                LocalDateTime.now();

        Reservation reservation =
                Reservation.builder()
                        .member(member)
                        .slot(slot)
                        .status(
                                ReservationStatus.RESERVED
                        )
                        .donorName(
                                request.getDonorName().trim()
                        )
                        .phone(
                                normalizePhone(
                                        request.getPhone()
                                )
                        )
                        .residentNumberHash(
                                residentNumberHashService.hash(
                                        request.getResidentRegistrationNumber()
                                )
                        )
                        .residentNumberMasked(
                                residentNumberHashService.mask(
                                        request.getResidentRegistrationNumber()
                                )
                        )
                        .privacyConsentedAt(
                                consentedAt
                        )
                        .privacyRetentionUntil(
                                consentedAt.plusYears(5)
                        )
                        .build();

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return new AdminReservationResponseDto(
                savedReservation
        );
    }

    private void cancelExpiredReservations(
            Member member
    ) {
        LocalDate today =
                LocalDate.now();

        List<Reservation> reservedReservations =
                reservationRepository
                        .findByMember_IdAndStatusOrderByIdDesc(
                                member.getId(),
                                ReservationStatus.RESERVED
                        );

        reservedReservations.stream()
                .filter(reservation ->
                        reservation.getSlot()
                                .getReservationDate()
                                .isBefore(today)
                )
                .forEach(reservation ->
                        reservation.setStatus(
                                ReservationStatus.CANCELED
                        )
                );
    }

    private void validateReservationPersonalInfo(
            ReservationCreateDto request
    ) {
        if (
                request.getDonorName() == null
                        || request.getDonorName()
                        .trim()
                        .isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "성명을 입력해주세요."
            );
        }

        if (
                request.getPhone() == null
                        || request.getPhone()
                        .trim()
                        .isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "휴대전화를 입력해주세요."
            );
        }

        if (
                request.getResidentRegistrationNumber() == null
                        || request.getResidentRegistrationNumber()
                        .trim()
                        .isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "주민등록번호를 입력해주세요."
            );
        }

        /*
         * 실제 주민번호 진위 검증은 하지 않고
         * 전자문진과 동일하게 13자리 형식만 확인합니다.
         */
        residentNumberHashService.normalize(
                request.getResidentRegistrationNumber()
        );

        if (!request.isPrivacyConsented()) {
            throw new IllegalArgumentException(
                    "개인정보 수집 및 이용에 동의해주세요."
            );
        }
    }

    private String normalizePhone(
            String phone
    ) {
        return phone == null
                ? ""
                : phone.replaceAll(
                "[^0-9]",
                ""
        );
    }

    private void validateDonationEligibility(
            ReservationSlot slot,
            String email
    ) {
        DonationEligibilityResponseDto eligibility =
                donationHistoryService
                        .getMyDonationEligibility(
                                email
                        );

        LocalDate nextAvailableDate =
                switch (slot.getDonationType()) {
                    case WHOLE_BLOOD ->
                            eligibility
                                    .wholeBloodNextAvailableDate();

                    case PLATELET ->
                            eligibility
                                    .plateletNextAvailableDate();

                    case PLASMA ->
                            eligibility
                                    .plasmaNextAvailableDate();

                    case PLATELET_PLASMA ->
                            eligibility
                                    .plateletPlasmaNextAvailableDate();
                };

        if (
                nextAvailableDate != null
                        && slot.getReservationDate()
                        .isBefore(
                                nextAvailableDate
                        )
        ) {
            String donationTypeName =
                    switch (slot.getDonationType()) {
                        case WHOLE_BLOOD ->
                                "전혈";

                        case PLATELET ->
                                "혈소판";

                        case PLASMA ->
                                "혈장";

                        case PLATELET_PLASMA ->
                                "혈소판혈장";
                    };

            throw new IllegalStateException(
                    "최근 헌혈 기록으로 인해 "
                            + donationTypeName
                            + "헌혈은 "
                            + nextAvailableDate
                            + "부터 예약할 수 있습니다."
            );
        }
    }

    /*
     * 로그인한 회원의 예약 내역 조회
     */
    public List<AdminReservationResponseDto> getMyReservations(
            String email
    ) {
        Member member =
                memberService.findByAuthenticationIdentifier(
                        email
                );

        /*
         * 예약일이 지난 RESERVED 예약을
         * 조회 시점에 자동 취소합니다.
         */
        cancelExpiredReservations(
                member
        );

        return reservationRepository
                .findByMember_EmailOrderByIdDesc(
                        member.getEmail()
                )
                .stream()
                .map(AdminReservationResponseDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public void validateOwnReservationForSlotSearch(
            Long reservationId,
            String email
    ) {
        if (reservationId == null) {
            return;
        }

        Member member =
                memberService
                        .findByAuthenticationIdentifier(
                                email
                        );

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "예약을 찾을 수 없습니다."
                                )
                        );

        if (
                !reservation
                        .getMember()
                        .getId()
                        .equals(
                                member.getId()
                        )
        ) {
            throw new IllegalStateException(
                    "본인의 예약만 변경할 수 있습니다."
            );
        }

        if (
                reservation.getStatus()
                        != ReservationStatus.RESERVED
        ) {
            throw new IllegalStateException(
                    "진행 중인 예약만 변경할 수 있습니다."
            );
        }
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

        Member member =
                memberService.findByAuthenticationIdentifier(
                        email
                );

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
                        .getId()
                        .equals(member.getId())
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

        ReservationSlot oldSlot =
                reservation.getSlot();

        /*
         * 사용자 예약 변경은
         * 기존 센터와 헌혈 종류는 유지하고,
         * 날짜와 시간은 변경할 수 있습니다.
         */
        if (
                !oldSlot.getCenter()
                        .getId()
                        .equals(
                                newSlot.getCenter().getId()
                        )
                        || oldSlot.getDonationType()
                        != newSlot.getDonationType()
        ) {
            throw new IllegalStateException(
                    "같은 센터와 헌혈 종류의 예약만 변경할 수 있습니다."
            );
        }

        LocalDate today =
                LocalDate.now();

        if (
                !newSlot.getReservationDate()
                        .isAfter(today)
        ) {
            throw new IllegalStateException(
                    "당일 및 지난 날짜에는 예약을 변경할 수 없습니다."
            );
        }

        LocalDate maxReservationDate =
                today.plusMonths(2);

        if (
                newSlot.getReservationDate()
                        .isAfter(maxReservationDate)
        ) {
            throw new IllegalStateException(
                    "예약은 오늘부터 2개월 이내의 날짜로만 변경할 수 있습니다."
            );
        }

        validateDonationEligibility(
                newSlot,
                member.getEmail()
        );

        validateEquipmentCapacity(
                newSlot,
                reservation.getId()
        );

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
        validateEquipmentCapacity(
                newSlot,
                reservation.getId()
        );

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
        Member member =
                memberService.findByAuthenticationIdentifier(
                        email
                );

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
                        .getId()
                        .equals(member.getId())
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

    private void validateEquipmentCapacity(
            ReservationSlot targetSlot,
            Long excludeReservationId
    ) {

        EquipmentGroup equipmentGroup =
                targetSlot.getEquipmentGroup();

        if (equipmentGroup == null) {
            throw new IllegalStateException(
                    "예약 슬롯의 장비그룹 정보가 없습니다."
            );
        }

        /*
         * 마지막 장비에 동시 예약이 들어오는 것을 막기 위해
         * 해당 센터 + 장비그룹 row를 먼저 잠급니다.
         */
        EquipmentGroup lockedGroup =
                equipmentGroupRepository
                        .findForUpdate(
                                targetSlot
                                        .getCenter()
                                        .getId(),
                                targetSlot
                                        .getDonationType()
                                        .getEquipmentGroupType()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "예약에 사용할 장비그룹을 찾을 수 없습니다."
                                )
                        );

        if (!lockedGroup.isActive()) {
            throw new IllegalStateException(
                    "현재 해당 헌혈 장비를 사용할 수 없습니다."
            );
        }

        LocalTime targetStart =
                targetSlot.getStartTime();

        LocalTime targetEnd =
                targetStart.plusMinutes(
                        targetSlot
                                .getDonationType()
                                .getEquipmentUsageMinutes()
                );

        List<Reservation> reservations =
                reservationRepository
                        .findEquipmentGroupReservations(
                                lockedGroup.getId(),
                                targetSlot.getReservationDate(),
                                ReservationStatus.RESERVED
                        );

        long concurrentUsage =
                EquipmentCapacityCalculator
                        .calculateMaxConcurrentUsage(
                                reservations,
                                targetStart,
                                targetEnd,
                                excludeReservationId
                        );

        if (
                concurrentUsage
                        >= lockedGroup.getCapacity()
        ) {
            throw new IllegalStateException(
                    "해당 시간에는 사용 가능한 헌혈 장비가 없습니다."
            );
        }
    }
}