package com.shop.changhyeon.reward.repository;

import com.shop.changhyeon.reward.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    boolean existsByReservation_Id(Long reservationId);

    List<PointTransaction> findByMember_IdOrderByCreatedAtDescIdDesc(Long memberId);

    @Query("select coalesce(sum(point.points), 0) from PointTransaction point where point.member.id = :memberId")
    Long sumPointsByMemberId(@Param("memberId") Long memberId);
}
