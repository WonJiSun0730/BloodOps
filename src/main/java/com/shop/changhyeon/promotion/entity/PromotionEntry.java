package com.shop.changhyeon.promotion.entity;

import com.shop.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_entry", uniqueConstraints = @UniqueConstraint(name = "uk_promotion_entry_member", columnNames = {"promotion_id", "member_id"}))
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "promotion_id", nullable = false) private Promotion promotion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id", nullable = false) private Member member;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "option_id", nullable = false) private PromotionOption option;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    public PromotionEntry(Promotion promotion, Member member, PromotionOption option) { this.promotion = promotion; this.member = member; this.option = option; }
}
