package org.cinos.core.technical_verification.dto;

import lombok.Builder;
import org.cinos.core.technical_verification.model.*;

@Builder
public record TechnicalVerificationDTO (
        BrakingSystemVerification brakingSystemVerification,
        ChassisVerification chassisVerification,
        DashboardAndIndicatorsVerification dashboardAndIndicatorsVerification,
        InteriorVerification interiorVerification,
        MotorVerification motorVerification,
        PaintAndBodyworkVerification paintAndBodyworkVerification,
        SuspensionAndSteeringVerification suspensionAndSteeringVerification,
        TiresAndWheelsVerification tiresAndWheelsVerification,
        Boolean isApproved
){}
