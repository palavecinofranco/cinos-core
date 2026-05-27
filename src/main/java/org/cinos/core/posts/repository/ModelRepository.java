package org.cinos.core.posts.repository;

import org.cinos.core.posts.entity.ModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<ModelEntity, Long> {

    List<ModelEntity> findByMake_Name(String makeName);

    Optional<ModelEntity> findByName(String name);
}
