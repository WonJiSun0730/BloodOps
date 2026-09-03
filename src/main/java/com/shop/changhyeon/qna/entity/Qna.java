package com.shop.changhyeon.qna.entity;

import com.shop.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "qna", indexes = {
        @Index(name = "idx_qna_created_at", columnList = "created_at"),
        @Index(name = "idx_qna_status", columnList = "answered")
})
@Getter
@NoArgsConstructor
public class Qna {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 150)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Lob
    private String answer;

    @Column(nullable = false)
    private boolean answered;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    public Qna(Member member, String title, String content) {
        this.member = member;
        updateQuestion(title, content);
    }

    public void updateQuestion(String title, String content) {
        this.title = title.trim();
        this.content = content.trim();
    }

    public void answer(String answer) {
        this.answer = answer.trim();
        this.answered = true;
        this.answeredAt = LocalDateTime.now();
    }
}
