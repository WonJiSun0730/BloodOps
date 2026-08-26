package com.shop.reservation.entity;

import com.shop.center.entity.Center;
import com.shop.common.entity.BaseEntity;
import com.shop.reservation.constant.DonationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "reservation_slot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_slot",
                        columnNames = {
                                "center_id",
                                "reservation_date",
                                "start_time",
                                "donation_type"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * 이 예약 슬롯이 어느 헌혈센터에 속하는지 저장합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "center_id",
            nullable = false
    )
    private Center center;


    /*
     * 예약 가능한 날짜입니다.
     */
    @Column(
            name = "reservation_date",
            nullable = false
    )
    private LocalDate reservationDate;


    /*
     * 슬롯 시작/종료 시간입니다.
     *
     * 예:
     * 09:00 ~ 09:30
     */
    @Column(
            name = "start_time",
            nullable = false
    )
    private LocalTime startTime;

    @Column(
            name = "end_time",
            nullable = false
    )
    private LocalTime endTime;


    /*
     * 전혈 / 혈소판 / 혈장 / 혈소판혈장
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "donation_type",
            nullable = false
    )
    private DonationType donationType;

    /*
     * 이 슬롯이 사용하는 공유 장비그룹입니다.
     *
     * 예:
     * 전혈 / 혈장
     * → WHOLE_BLOOD_PLASMA 장비그룹 공유
     *
     * 혈소판 / 혈소판혈장
     * → PLATELET 장비그룹 공유
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "equipment_group_id",
            nullable = false
    )
    private EquipmentGroup equipmentGroup;


    /*
     * 해당 시간대에 받을 수 있는 최대 예약 인원입니다.
     */
    @Column(nullable = false)
    private int capacity;


    /*
     * 센터 사정 등으로 예약을 막아야 할 경우 false로 변경합니다.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}