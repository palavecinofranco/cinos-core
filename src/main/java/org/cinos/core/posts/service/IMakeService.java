package org.cinos.core.posts.service;

import org.cinos.core.posts.dto.MakeDTO;
import org.cinos.core.posts.entity.MakeEntity;

import java.util.List;
import java.util.Optional;

public interface IMakeService {
    Optional<MakeEntity> findByName(String name);
    List<MakeDTO> findByNameContainingIgnoreCase(String name);
}
