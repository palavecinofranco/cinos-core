package org.cinos.core.technical_verification.dto;

import lombok.Builder;

@Builder
public record TechnicalVerificationPercentsDTO(
        Double brakingSystemVerification,
        Double chassisVerification,
        Double dashboardAndIndicatorsVerification,
        Double interiorVerification,
        Double motorVerification,
        Double paintAndBodyworkVerification,
        Double suspensionAndSteeringVerification,
        Double tiresAndWheelsVerification
) {

}
