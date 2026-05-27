package org.cinos.core.posts.repository;

import org.cinos.core.posts.entity.MakeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MakeRepository extends JpaRepository<MakeEntity, Long> {
    Optional<MakeEntity> findByName(String name);
    List<MakeEntity> findByNameContainingIgnoreCase(String name);

}
