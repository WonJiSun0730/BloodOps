package com.shop.halloffame.repository;

import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.halloffame.dto.DonationCountProjection;
import com.shop.reservation.constant.DonationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HallOfFameRepository
        extends Repository<DonationHistory, Long> {

    @Query("""
            select
                h.member.id as memberId,
                h.member.name as memberName,
                count(h.id) as donationCount
            from DonationHistory h
            group by
                h.member.id,
                h.member.name
            order by
                count(h.id) desc,
                h.member.id asc
            """)
    List<DonationCountProjection>
    findOverallRanking(
            Pageable pageable
    );


    @Query("""
            select
                h.member.id as memberId,
                h.member.name as memberName,
                count(h.id) as donationCount
            from DonationHistory h
            where h.donationType = :donationType
            group by
                h.member.id,
                h.member.name
            order by
                count(h.id) desc,
                h.member.id asc
            """)
    List<DonationCountProjection>
    findRankingByDonationType(
            @Param("donationType")
            DonationType donationType,

            Pageable pageable
    );


    long countByMember_Id(
            Long memberId
    );


    long countByMember_IdAndDonationType(
            Long memberId,
            DonationType donationType
    );
}