package org.cinos.core.technical_verification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cinos.core.posts.entity.PostEntity;
import org.cinos.core.posts.models.VerificationStatus;
import org.cinos.core.technical_verification.model.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "technical_verifications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TechnicalVerification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private PostEntity post;
    private LocalDateTime sentToVerificationDate;
    private LocalDateTime verificationAcceptedDate;
    private LocalDateTime verificationAppointmentDate;
    private LocalDateTime verificationMadeDate;
    @Embedded
    private InteriorVerification interiorVerification;
    @Embedded
    private MotorVerification motorVerification;
    @Embedded
    private ChassisVerification chassisVerification;
    @Embedded
    private SuspensionAndSteeringVerification suspensionAndSteeringVerification;
    @Embedded
    private BrakingSystemVerification brakingSystemVerification;
    @Embedded
    private TiresAndWheelsVerification tiresAndWheelsVerification;
    @Embedded
    private PaintAndBodyworkVerification paintAndBodyworkVerification;
    @Embedded
    private DashboardAndIndicatorsVerification dashboardAndIndicatorsVerification;
    @Enumerated(EnumType.STRING)
    private VerificationStatus status;
    private Boolean isApproved;


}
