package org.cinos.core.posts.repository;

import org.cinos.core.posts.entity.PostImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImageEntity, Long> {
}
