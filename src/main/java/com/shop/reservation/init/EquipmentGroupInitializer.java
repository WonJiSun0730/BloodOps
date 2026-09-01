package com.shop.reservation.init;

import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.reservation.constant.EquipmentGroupType;
import com.shop.reservation.entity.EquipmentGroup;
import com.shop.reservation.entity.ReservationSlot;
import com.shop.reservation.repository.EquipmentGroupRepository;
import com.shop.reservation.repository.ReservationSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
public class EquipmentGroupInitializer
        implements CommandLineRunner {

    private static final int
            DEFAULT_CAPACITY = 2;

    private final CenterRepository
            centerRepository;

    private final EquipmentGroupRepository
            equipmentGroupRepository;

    private final ReservationSlotRepository
            reservationSlotRepository;


    @Override
    @Transactional
    public void run(String... args) {

        List<Center> centers =
                centerRepository.findAll();

        for (Center center : centers) {

            createIfMissing(
                    center,
                    EquipmentGroupType
                            .WHOLE_BLOOD_PLASMA
            );

            createIfMissing(
                    center,
                    EquipmentGroupType
                            .PLATELET
            );
        }

        connectExistingSlots();
    }


    private void createIfMissing(
            Center center,
            EquipmentGroupType groupType
    ) {

        boolean exists =
                equipmentGroupRepository
                        .findByCenter_IdAndGroupType(
                                center.getId(),
                                groupType
                        )
                        .isPresent();

        if (exists) {
            return;
        }

        EquipmentGroup equipmentGroup =
                EquipmentGroup.builder()
                        .center(center)
                        .groupType(groupType)
                        .capacity(
                                DEFAULT_CAPACITY
                        )
                        .active(true)
                        .build();

        equipmentGroupRepository.save(
                equipmentGroup
        );
    }

    private void connectExistingSlots() {

        List<ReservationSlot> slots =
                reservationSlotRepository
                        .findAll();

        for (ReservationSlot slot : slots) {

            /*
             * 이미 장비그룹이 연결된 신규 슬롯은
             * 다시 처리하지 않습니다.
             */
            if (slot.getEquipmentGroup() != null) {
                continue;
            }

            EquipmentGroup equipmentGroup =
                    equipmentGroupRepository
                            .findByCenter_IdAndGroupType(
                                    slot.getCenter().getId(),
                                    slot.getDonationType()
                                            .getEquipmentGroupType()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "기존 예약 슬롯에 연결할 장비그룹을 찾을 수 없습니다."
                                    )
                            );

            slot.setEquipmentGroup(
                    equipmentGroup
            );
        }
    }
}