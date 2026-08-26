package com.shop.reservation.entity;

import com.shop.center.entity.Center;
import com.shop.common.entity.BaseEntity;
import com.shop.reservation.constant.EquipmentGroupType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "equipment_group",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_equipment_group_center_type",
                        columnNames = {
                                "center_id",
                                "group_type"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentGroup
        extends BaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    /*
     * 장비그룹이 속한 헌혈센터입니다.
     *
     * 센터마다 같은 장비그룹 유형을
     * 하나씩 가질 수 있습니다.
     */
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "center_id",
            nullable = false
    )
    private Center center;


    /*
     * 공유 장비 유형입니다.
     *
     * WHOLE_BLOOD_PLASMA:
     * 전혈 + 혈장이 공유하는 장비
     *
     * PLATELET:
     * 혈소판 + 혈소판혈장이 공유하는 장비
     */
    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "group_type",
            nullable = false,
            length = 50
    )
    private EquipmentGroupType
            groupType;


    /*
     * 해당 센터에서 이 장비그룹으로
     * 동시에 처리할 수 있는 최대 인원입니다.
     *
     * 예:
     * 혈소판 장비 2대
     * → capacity = 2
     */
    @Column(
            nullable = false
    )
    private int capacity;


    /*
     * 장비 점검/운영중단 등으로
     * 예약에 사용하지 않을 경우 false.
     */
    @Builder.Default
    @Column(
            nullable = false
    )
    private boolean active = true;
}