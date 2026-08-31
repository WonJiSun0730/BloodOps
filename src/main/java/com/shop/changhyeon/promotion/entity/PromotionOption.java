package com.shop.changhyeon.promotion.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotion_option")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "promotion_id", nullable = false) private Promotion promotion;
    @Column(nullable = false, length = 120) private String label;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    PromotionOption(Promotion promotion, String label, int displayOrder) { this.promotion = promotion; this.label = label; this.displayOrder = displayOrder; }
}
