package org.cinos.core.posts.repository;

import org.cinos.core.posts.entity.PostLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLocationRepository extends JpaRepository<PostLocationEntity, Long> {
}
