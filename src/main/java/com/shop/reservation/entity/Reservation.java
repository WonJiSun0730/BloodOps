package com.shop.reservation.entity;

import com.shop.common.entity.BaseEntity;
import com.shop.member.entity.Member;
import com.shop.reservation.constant.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    /*
     * 예약 당시 예약자 정보의 스냅샷입니다.
     * 회원정보가 이후 변경되어도 예약 당시 값을 유지합니다.
     */
    @Column(name = "donor_name", length = 100)
    private String donorName;

    @Column(name = "phone", length = 30)
    private String phone;

    /*
     * 주민등록번호 원문은 저장하지 않고
     * 전자문진과 동일하게 hash + masked 값만 저장합니다.
     */
    @Column(name = "resident_number_hash", length = 64)
    private String residentNumberHash;

    @Column(name = "resident_number_masked", length = 14)
    private String residentNumberMasked;

    /*
     * 개인정보 수집·이용 동의 시각
     */
    @Column(name = "privacy_consented_at")
    private LocalDateTime privacyConsentedAt;

    /*
     * 예약 개인정보 보유 만료 시각
     */
    @Column(name = "privacy_retention_until")
    private LocalDateTime privacyRetentionUntil;
}