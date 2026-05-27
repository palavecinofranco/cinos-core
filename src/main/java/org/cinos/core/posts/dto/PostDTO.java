package org.cinos.core.posts.dto;
import lombok.Builder;
import org.cinos.core.technical_verification.dto.TechnicalVerificationDTO;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record PostDTO(
        Long id,
        String make,
        String model,
        String year,
        Double price,
        Boolean isUsed,
        Long userId,
        ZonedDateTime publicationDate,
        String userFullName,
        Boolean active,
        String currencySymbol,
        String kilometers,
        String fuel,
        String transmission,
        PostLocationDTO location,
        List<String> imagesUrls,
        String userAvatar,
        TechnicalVerificationDTO technicalVerification,
        Boolean isVerified,
        Integer hp,
        String motor,
        String traccion,
        String userPhone,
        String userAttentionHours) {
}
