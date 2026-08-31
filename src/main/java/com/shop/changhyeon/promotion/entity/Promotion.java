package com.shop.changhyeon.promotion.entity;

import com.shop.changhyeon.promotion.constant.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "promotion", indexes = {
        @Index(name = "idx_promotion_active_period", columnList = "active,start_date,end_date"),
        @Index(name = "idx_promotion_category", columnList = "category")
})
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Promotion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PromotionCategory category;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PromotionType type;
    @Column(name = "region_name", length = 50) private String regionName;
    @Column(nullable = false, length = 150) private String title;
    @Column(nullable = false, length = 300) private String summary;
    @Lob @Column(nullable = false) private String content;
    @Column(name = "image_url", length = 1000) private String imageUrl;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date", nullable = false) private LocalDate endDate;
    @Column(name = "donation_eligibility_start_date") private LocalDate donationEligibilityStartDate;
    @Column(nullable = false) private boolean active = true;
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder asc, id asc") private List<PromotionOption> options = new ArrayList<>();
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;

    public void replaceOptions(List<String> labels) {
        options.clear();
        for (int i = 0; i < labels.size(); i++) options.add(new PromotionOption(this, labels.get(i), i));
    }
}
