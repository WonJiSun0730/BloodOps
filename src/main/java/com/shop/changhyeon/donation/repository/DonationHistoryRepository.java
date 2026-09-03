package com.shop.changhyeon.donation.repository;

import com.shop.changhyeon.donation.entity.DonationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {

    List<DonationHistory> findByMember_IdOrderByDonationDateDescIdDesc(Long memberId);

    List<DonationHistory> findAllByOrderByRegTimeDescIdDesc();

    @Query("""
        select h
        from DonationHistory h
        join fetch h.member m
        join fetch h.reservation r
        join fetch r.slot s
        join fetch s.center c
        where (:centerId is null or c.id = :centerId)
        order by h.regTime desc, h.id desc
        """)
    List<DonationHistory> findMedicalDonationHistory(
            @Param("centerId") Long centerId
    );

    boolean existsByMember_IdAndDonationDateBetween(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<DonationHistory> findByMember_IdAndDonationDateGreaterThanEqualOrderByDonationDateAscIdAsc(
            Long memberId,
            LocalDate startDate
    );
}
