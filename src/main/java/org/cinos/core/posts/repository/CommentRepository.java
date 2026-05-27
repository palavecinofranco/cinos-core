package org.cinos.core.posts.repository;

import org.cinos.core.posts.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPostId(Long postId);
    Page<CommentEntity> findByPostId(Long postId, Pageable pageable);
}
