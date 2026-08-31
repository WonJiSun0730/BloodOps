package com.shop.center.service;

import com.shop.center.constant.CenterSource;
import com.shop.center.dto.CenterCreateRequestDto;
import com.shop.center.dto.CenterMapResponseDto;
import com.shop.center.dto.CenterResponseDto;
import com.shop.center.dto.CenterUpdateRequestDto;
import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.favorite.repository.FavoriteCenterRepository;
import com.shop.reservation.repository.EquipmentGroupRepository;
import com.shop.reservation.repository.ReservationRepository;
import com.shop.reservation.repository.ReservationSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CenterService {

    private final CenterRepository centerRepository;

    private final ReservationSlotRepository
            reservationSlotRepository;

    private final EquipmentGroupRepository
            equipmentGroupRepository;

    private final FavoriteCenterRepository
            favoriteCenterRepository;

    private final ReservationRepository
            reservationRepository;


    public List<CenterResponseDto> getCenters(String keyword) {

        if (keyword == null || keyword.isBlank()) {

            return centerRepository.findAll()
                    .stream()
                    .map(CenterResponseDto::from)
                    .toList();
        }

        String trimmedKeyword = keyword.trim();

        List<CenterResponseDto> nameMatches =
                centerRepository
                        .findByCenterNameContainingIgnoreCase(trimmedKeyword)
                        .stream()
                        .map(CenterResponseDto::from)
                        .toList();

        if (!nameMatches.isEmpty()) {
            return nameMatches;
        }

        return centerRepository
                .findByAddressContainingIgnoreCase(trimmedKeyword)
                .stream()
                .map(CenterResponseDto::from)
                .toList();
    }

    public Page<CenterResponseDto> getCenterPage(
            String keyword,
            int page
    ) {

        int safePage =
                Math.max(page, 0);

        Pageable pageable =
                PageRequest.of(
                        safePage,
                        10,
                        Sort.by(
                                Sort.Order.asc("centerName"),
                                Sort.Order.asc("id")
                        )
                );

        if (keyword == null || keyword.isBlank()) {

            return centerRepository
                    .findAll(pageable)
                    .map(CenterResponseDto::from);
        }

        String trimmedKeyword =
                keyword.trim();

        return centerRepository
                .findByCenterNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                        trimmedKeyword,
                        trimmedKeyword,
                        pageable
                )
                .map(CenterResponseDto::from);
    }


    public List<CenterMapResponseDto>
    getCenterMapCenters(
            String keyword
    ) {

        List<Center> centers;

        if (keyword == null || keyword.isBlank()) {

            centers =
                    centerRepository.findAll();

        } else {

            String trimmedKeyword =
                    keyword.trim();

            centers =
                    centerRepository
                            .findByCenterNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                                    trimmedKeyword,
                                    trimmedKeyword
                            );
        }

        return centers.stream()
                .map(CenterMapResponseDto::from)
                .toList();
    }

    public Page<CenterResponseDto> getAdminCenters(
            String keyword,
            Pageable pageable
    ) {

        if (keyword == null || keyword.isBlank()) {
            return centerRepository
                    .findAll(pageable)
                    .map(CenterResponseDto::from);
        }

        String trimmedKeyword =
                keyword.trim();

        Page<Center> nameMatches =
                centerRepository
                        .findByCenterNameContainingIgnoreCase(
                                trimmedKeyword,
                                pageable
                        );

        if (nameMatches.hasContent()) {
            return nameMatches
                    .map(CenterResponseDto::from);
        }

        return centerRepository
                .findByAddressContainingIgnoreCase(
                        trimmedKeyword,
                        pageable
                )
                .map(CenterResponseDto::from);
    }
    
    public CenterResponseDto getCenter(Long centerId) {

        return centerRepository.findById(centerId)
                .map(CenterResponseDto::from)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "헌혈센터를 찾을 수 없습니다."
                        )
                );
    }

    @Transactional
    public CenterResponseDto updateCenter(
            Long centerId,
            CenterUpdateRequestDto request
    ) {

        Center center =
                centerRepository.findById(centerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "헌혈센터를 찾을 수 없습니다."
                                )
                        );


        center.setActive(
                request.isActive()
        );

        center.setWeekdayHours(
                request.getWeekdayHours()
        );

        center.setSaturdayHours(
                request.getSaturdayHours()
        );

        center.setSundayHours(
                request.getSundayHours()
        );

        center.setHolidayHours(
                request.getHolidayHours()
        );

        center.setDonationTypes(
                request.getDonationTypes()
        );

        center.setAmenities(
                request.getAmenities()
        );

        center.setDirections(
                request.getDirections()
        );

        center.setParkingInfo(
                request.getParkingInfo()
        );


        return CenterResponseDto.from(center);
    }

    @Transactional
    public CenterResponseDto createCenter(
            CenterCreateRequestDto request
    ) {

        Center center = new Center();


        center.setBloodCenterName(
                request.getBloodCenterName()
        );

        center.setCenterName(
                request.getCenterName()
        );

        center.setCenterType(
                request.getCenterType()
        );

        center.setAddress(
                request.getAddress()
        );

        center.setPhone(
                request.getPhone()
        );


        center.setSource(
                CenterSource.ADMIN
        );

        center.setActive(
                request.isActive()
        );


        center.setWeekdayHours(
                request.getWeekdayHours()
        );

        center.setSaturdayHours(
                request.getSaturdayHours()
        );

        center.setSundayHours(
                request.getSundayHours()
        );

        center.setHolidayHours(
                request.getHolidayHours()
        );


        center.setDonationTypes(
                request.getDonationTypes()
        );

        center.setAmenities(
                request.getAmenities()
        );

        center.setDirections(
                request.getDirections()
        );

        center.setParkingInfo(
                request.getParkingInfo()
        );


        Center savedCenter =
                centerRepository.save(center);


        return CenterResponseDto.from(
                savedCenter
        );
    }

    @Transactional
    public void deleteCenter(
            Long centerId
    ) {

        Center center =
                centerRepository.findById(centerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "헌혈센터를 찾을 수 없습니다."
                                )
                        );

        /*
         * 실제 예약 이력이 하나라도 있으면
         * 센터 자체는 삭제하지 않습니다.
         *
         * RESERVED / CANCELED 여부와 관계없이
         * 과거 예약 기록 보존을 우선합니다.
         */
        boolean hasReservationHistory =
                reservationRepository
                        .existsBySlot_Center_Id(centerId);

        if (hasReservationHistory) {
            throw new IllegalStateException(
                    "예약 이력이 존재하는 센터는 삭제할 수 없습니다. 운영 중단으로 변경해주세요."
            );
        }

        /*
         * 예약 이력이 없는 센터는
         * 센터를 참조하는 부속 데이터를 먼저 정리합니다.
         */
        favoriteCenterRepository
                .deleteByCenter_Id(centerId);

        reservationSlotRepository
                .deleteByCenter_Id(centerId);

        equipmentGroupRepository
                .deleteByCenter_Id(centerId);

        centerRepository.delete(center);
    }
}