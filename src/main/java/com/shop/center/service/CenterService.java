package com.shop.center.service;

import com.shop.center.constant.CenterSource;
import com.shop.center.dto.CenterCreateRequestDto;
import com.shop.center.dto.CenterResponseDto;
import com.shop.center.dto.CenterUpdateRequestDto;
import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CenterService {

    private final CenterRepository centerRepository;


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

}