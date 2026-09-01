package com.shop.favorite.entity;

import com.shop.center.entity.Center;
import com.shop.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "favorite_center",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_favorite_center_member_center",
                        columnNames = {
                                "member_id",
                                "center_id"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(
        access = AccessLevel.PROTECTED
)
public class FavoriteCenter {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "center_id",
            nullable = false
    )
    private Center center;

    public FavoriteCenter(
            Member member,
            Center center
    ) {
        this.member = member;
        this.center = center;
    }
}