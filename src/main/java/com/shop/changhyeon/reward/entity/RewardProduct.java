package com.shop.changhyeon.reward.entity;

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

    public void updateImage(byte[] imageData, String imageContentType) {
        this.imageData = imageData;
        this.imageContentType = imageContentType;
    }

    public boolean hasImage() {
        return imageData != null && imageData.length > 0;
    }

    public void update(String name, String description, int pointPrice, int stockQuantity, boolean active) {
        this.name = name;
        this.description = description;
        this.pointPrice = pointPrice;
        this.stockQuantity = stockQuantity;
        this.active = active;
    }
}
