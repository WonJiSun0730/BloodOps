package com.shop.changhyeon.reward.entity;

import com.shop.changhyeon.reward.constant.RewardProductType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RewardProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "point_price", nullable = false)
    private int pointPrice;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30,
            columnDefinition = "varchar(30) default 'PHYSICAL'")
    @Builder.Default
    private RewardProductType productType = RewardProductType.PHYSICAL;

    @Column(nullable = false)
    private boolean active;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @Column(name = "image_content_type", length = 100)
    private String imageContentType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void decreaseStock() {
        if (stockQuantity <= 0) {
            throw new IllegalStateException("품절된 상품입니다.");
        }
        stockQuantity--;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("입고 수량은 1개 이상이어야 합니다.");
        }
        stockQuantity += quantity;
    }

    public void updateImage(byte[] imageData, String imageContentType) {
        this.imageData = imageData;
        this.imageContentType = imageContentType;
    }

    public boolean hasImage() {
        return imageData != null && imageData.length > 0;
    }

    public void update(
            String name,
            String description,
            int pointPrice,
            int stockQuantity,
            RewardProductType productType,
            boolean active
    ) {
        this.name = name;
        this.description = description;
        this.pointPrice = pointPrice;
        this.stockQuantity = stockQuantity;
        this.productType = productType;
        this.active = active;
    }
}
