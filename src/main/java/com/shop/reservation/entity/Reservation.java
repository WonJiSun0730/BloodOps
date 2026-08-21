package com.shop.reservation.entity;

import com.shop.common.entity.BaseEntity;
import com.shop.member.entity.Member;
import com.shop.reservation.constant.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * 예약한 회원입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;


    /*
     * 회원이 선택한 예약 슬롯입니다.
     *
     * 슬롯을 통해 센터, 날짜, 시간,
     * 헌혈종류 정보를 확인할 수 있습니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "slot_id",
            nullable = false
    )
    private ReservationSlot slot;


    /*
     * ADMIN 예약 운영에서 사용하는 예약 상태입니다.
     *
     * 헌혈 진행 상태와는 별도로 관리합니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status =
            ReservationStatus.RESERVED;
}