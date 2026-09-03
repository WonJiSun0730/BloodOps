package com.shop.changhyeon.qna.repository;

import com.shop.changhyeon.qna.entity.Qna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QnaRepository extends JpaRepository<Qna, Long> {
    @EntityGraph(attributePaths = "member")
    Page<Qna> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String titleKeyword,
            String contentKeyword,
            Pageable pageable
    );

    @Override
    @EntityGraph(attributePaths = "member")
    Optional<Qna> findById(Long id);
}
