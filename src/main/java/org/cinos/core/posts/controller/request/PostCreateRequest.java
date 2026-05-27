package org.cinos.core.posts.controller.request;

import org.cinos.core.posts.dto.PostLocationDTO;
import org.cinos.core.posts.models.CurrencySymbol;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record PostCreateRequest(
        String make,
        String model,
        String year,
        Boolean isUsed,
        Double price,
        String fuel,
        String transmission,
        Long userId,
        Boolean active,
        String kilometers,
        PostLocationDTO location,
        CurrencySymbol currencySymbol,
        Integer hp,
        String motor,
        String traccion) implements Serializable {
}
