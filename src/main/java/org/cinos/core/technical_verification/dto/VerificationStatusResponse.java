package org.cinos.core.technical_verification.dto;

import lombok.Builder;
import org.cinos.core.posts.models.VerificationStatus;

import java.time.LocalDateTime;

@Builder
public record VerificationStatusResponse(
        LocalDateTime sentToVerificationDate,
        LocalDateTime verificationAcceptedDate,
        LocalDateTime verificationAppointmentDate,
        LocalDateTime verificationMadeDate,
        Boolean isApproved,
        VerificationStatus status
) {

}
