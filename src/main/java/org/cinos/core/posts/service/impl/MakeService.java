package org.cinos.core.posts.service.impl;

import lombok.RequiredArgsConstructor;
import org.cinos.core.posts.dto.MakeDTO;
import org.cinos.core.posts.entity.MakeEntity;
import org.cinos.core.posts.repository.MakeRepository;
import org.cinos.core.posts.service.IMakeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MakeService implements IMakeService {
    private final MakeRepository makeRepository;

    @Override
    public Optional<MakeEntity> findByName(String name) {
        return makeRepository.findByName(name);
    }

    @Override
    public List<MakeDTO> findByNameContainingIgnoreCase(String name) {
        if (!StringUtils.hasText(name)){
            return makeRepository.findAll().stream()
                    .sorted(Comparator.comparing(MakeEntity::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(e->
                    MakeDTO.builder()
                            .id(e.getId())
                            .name(e.getName())
                            .build()
            ).toList();
        }

        return makeRepository.findByNameContainingIgnoreCase(name).stream()
                .sorted(Comparator.comparing(MakeEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(e->
                MakeDTO.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build()
        ).toList();
    }
}
