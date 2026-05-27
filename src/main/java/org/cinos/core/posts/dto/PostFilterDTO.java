package org.cinos.core.posts.dto;

import java.util.List;

public record PostFilterDTO(
        List<String> make,
        List<String> model,
        String search,
        String minYear,
        String maxYear,
        String fuelType,
        String transmission,
        Double minPrice,
        Double maxPrice,
        Integer minMileage,
        Integer maxMileage,
        Boolean isUsed,
        Integer page,
        Integer size
) {
}
