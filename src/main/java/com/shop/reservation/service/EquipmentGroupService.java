package com.shop.reservation.service;

import com.shop.reservation.dto.AdminEquipmentGroupResponseDto;
import com.shop.reservation.dto.AdminEquipmentGroupUpdateDto;
import com.shop.reservation.entity.EquipmentGroup;
import com.shop.reservation.repository.EquipmentGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentGroupService {

    private final EquipmentGroupRepository
            equipmentGroupRepository;


    /*
     * 특정 센터의 장비그룹 전체 조회.
     *
     * 현재 구조에서는 센터당
     * WHOLE_BLOOD_PLASMA / PLATELET
     * 두 그룹이 존재합니다.
     */
    @Transactional(readOnly = true)
    public List<AdminEquipmentGroupResponseDto>
    getEquipmentGroups(
            Long centerId
    ) {

        if (centerId == null) {
            throw new IllegalArgumentException(
                    "헌혈센터를 선택해주세요."
            );
        }

        return equipmentGroupRepository
                .findByCenter_IdOrderByGroupTypeAsc(
                        centerId
                )
                .stream()
                .map(
                        AdminEquipmentGroupResponseDto::new
                )
                .toList();
    }


    /*
     * 관리자 장비그룹 수정.
     *
     * 장비 수량과 운영 여부를 수정할 수 있습니다.
     */
    public AdminEquipmentGroupResponseDto
    updateEquipmentGroup(
            Long equipmentGroupId,
            AdminEquipmentGroupUpdateDto request
    ) {

        if (equipmentGroupId == null) {
            throw new IllegalArgumentException(
                    "장비그룹을 선택해주세요."
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "변경할 내용을 입력해주세요."
            );
        }


        EquipmentGroup equipmentGroup =
                equipmentGroupRepository
                        .findById(
                                equipmentGroupId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "장비그룹을 찾을 수 없습니다."
                                )
                        );


        if (
                request.getCapacity() == null
                        && request.getActive() == null
        ) {
            throw new IllegalArgumentException(
                    "변경할 내용을 입력해주세요."
            );
        }


        if (request.getCapacity() != null) {

            if (request.getCapacity() < 0) {
                throw new IllegalArgumentException(
                        "장비 수량은 0대 이상이어야 합니다."
                );
            }

            equipmentGroup.setCapacity(
                    request.getCapacity()
            );
        }


        if (request.getActive() != null) {
            equipmentGroup.setActive(
                    request.getActive()
            );
        }


        return new AdminEquipmentGroupResponseDto(
                equipmentGroup
        );
    }
}