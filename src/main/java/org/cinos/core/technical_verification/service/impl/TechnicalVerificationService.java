package org.cinos.core.technical_verification.service.impl;

import lombok.RequiredArgsConstructor;
//import org.cinos.core.mail.models.SendEmailRequest;
//import org.cinos.core.mail.service.MailService;
import org.cinos.core.technical_verification.dto.OrderVerificationRequest;
import org.cinos.core.technical_verification.dto.TechnicalVerificationPercentsDTO;
import org.cinos.core.technical_verification.dto.TechnicalVerificationRequest;
import org.cinos.core.posts.entity.PostEntity;
import org.cinos.core.posts.models.VerificationStatus;
import org.cinos.core.posts.repository.PostRepository;
import org.cinos.core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.core.technical_verification.dto.VerificationStatusResponse;
import org.cinos.core.technical_verification.entity.TechnicalVerification;
import org.cinos.core.technical_verification.repository.TechnicalVerificationRepository;
import org.cinos.core.technical_verification.service.ITechnicalVerificationService;
import org.cinos.core.users.entity.UserEntity;
import org.cinos.core.users.model.Role;
import org.cinos.core.utils.exceptions.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.cinos.core.users.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class TechnicalVerificationService implements ITechnicalVerificationService {

    private final PostRepository postRepository;
    //private final MailService mailService;
    private final TechnicalVerificationRepository technicalVerificationRepository;
    private final UserRepository userRepository;

    @Override
    public void orderVerification(final OrderVerificationRequest orderVerificationRequest) throws PostNotFoundException {
        PostEntity post = postRepository.findById(orderVerificationRequest.postId()).orElseThrow(()->new PostNotFoundException("No se encontró la publicación con id " + orderVerificationRequest.postId()));

        UserEntity user = post.getUserAccount().getUser();
        if (user.getRoles() == null || !user.getRoles().contains(Role.PREMIUM)) {
            throw new BusinessException("Solo los usuarios premium pueden solicitar una verificación técnica.");
        }

        // Validar que el usuario tenga créditos de verificación técnica disponibles
        if (user.getTechnicalVerificationCredits() == null || user.getTechnicalVerificationCredits() <= 0) {
            throw new BusinessException("No tenés verificaciones técnicas disponibles. Necesitás créditos para solicitar una verificación.");
        }

        /*String message = """
                <p>Pedido de verificación técnica para <strong>%s %s</strong></p>
                <p><strong>Datos del vehiculo:</strong></p>
                <ul>
                    <li>Año: %s</li>
                    <li>Kilómetros: %skm</li>
                    <li>Transmisión: %s</li>
                    <li>Combustible: %s</li>
                </ul>
                <p><strong>Datos del usuario:</strong></p>
                <ul>
                    <li>Nombre: %s %s</li>
                    <li>Email: %s</li>
                    <li>Teléfono: %s</li>
                    <li>Ubicación: %s</li>
                </ul>
        """.formatted(post.getMake(), post.getModel(), post.getYear(), post.getKilometers(), post.getTransmission(), post.getFuel(), orderVerificationRequest.userPhone(), user.getLastname(), user.getEmail(), user.getPhone(), post.getLocation().getAddress());
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .to(new String[]{"fpalavecino@cinos.org"})
                .subject("Verificación pedida para la publicación " + post.getId())
                .message(message)
                .build();
        mailService.sendMail(sendEmailRequest);*/

        TechnicalVerification technicalVerification = post.getTechnicalVerification();
        technicalVerification.setStatus(VerificationStatus.SENT);
        technicalVerification.setSentToVerificationDate(LocalDateTime.now());
        technicalVerificationRepository.save(technicalVerification);

        // Consumir un crédito de verificación técnica
        user.setTechnicalVerificationCredits(user.getTechnicalVerificationCredits() - 1);
        userRepository.save(user);
    }

    @Override
    public void acceptVerification(final Long postId, final LocalDateTime verificationAppointment) throws PostNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException("No se encontró la publicación con id " + postId));
        if (post.getTechnicalVerification().getVerificationAcceptedDate() != null) {
            throw new BusinessException("La verificación ya fue aceptada para esta publicación");
        }
        post.getTechnicalVerification().setStatus(VerificationStatus.PENDING);
        post.getTechnicalVerification().setVerificationAcceptedDate(LocalDateTime.now());
        post.getTechnicalVerification().setVerificationAppointmentDate(verificationAppointment);
        postRepository.save(post);

        /*String message = """
                <p>Tu solicitud para verificación técnica para %s %s ha sido aceptada.</p>
                <ul>
                    <li>Fecha de visita del técnico: %s</li>
                </ul>
                """.formatted(post.getMake(), post.getModel(), verificationAppointment.toString());
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .to(new String[]{post.getUserAccount().getUser().getEmail()})
                .subject("Verificación técnica aceptada")
                .message(message)
                .build();
        String messageToTechnician = """
                <p>Has aceptado la verificación técnica para %s %s</p>
                <ul>
                    <li>Fecha de visita: %s</li>
                </ul>
                <p><strong>Datos del usuario:</strong></p>
                <ul>
                    <li>Nombre: %s %s</li>
                    <li>Email: %s</li>
                    <li>Teléfono: %s</li>
                    <li>Ubicación: %s</li>
                </ul>
                """.formatted(post.getMake(), post.getModel(), verificationAppointment.toString(), post.getUserAccount().getUser().getName(), post.getUserAccount().getUser().getLastname(), post.getUserAccount().getUser().getEmail(), post.getUserAccount().getUser().getPhone(), post.getLocation().getAddress());
        SendEmailRequest sendEmailRequestToTechnician = SendEmailRequest.builder()
                .to(new String[]{"fpalavecino@cinos.org"})
                .subject("Verificación técnica aceptada")
                .message(messageToTechnician)
                .build();
        mailService.sendMail(sendEmailRequest);
        mailService.sendMail(sendEmailRequestToTechnician);*/
    }

    @Override
    public void processVerification(TechnicalVerificationRequest request) throws PostNotFoundException {
        PostEntity post = postRepository.findById(request.postId()).orElseThrow(() -> new PostNotFoundException("No se encontró la publicación con id " + request.postId()));
        TechnicalVerification technicalVerification = post.getTechnicalVerification();
        if (technicalVerification.getVerificationAcceptedDate() == null) {
            throw new BusinessException("La verificación no fue aceptada para esta publicación");
        }

        technicalVerification.setMotorVerification(request.motorVerification());
        technicalVerification.setChassisVerification(request.chassisVerification());
        technicalVerification.setSuspensionAndSteeringVerification(request.suspensionAndSteeringVerification());
        technicalVerification.setBrakingSystemVerification(request.brakingSystemVerification());
        technicalVerification.setTiresAndWheelsVerification(request.tiresAndWheelsVerification());
        technicalVerification.setPaintAndBodyworkVerification(request.paintAndBodyworkVerification());
        technicalVerification.setDashboardAndIndicatorsVerification(request.dashboardAndIndicatorsVerification());
        technicalVerification.setInteriorVerification(request.interiorVerification());
        technicalVerification.setVerificationMadeDate(LocalDateTime.now());

        double totalScore =
                request.motorVerification().averageScore() +
                        request.chassisVerification().averageScore() +
                        request.suspensionAndSteeringVerification().averageScore() +
                        request.brakingSystemVerification().averageScore() +
                        request.tiresAndWheelsVerification().averageScore() +
                        request.paintAndBodyworkVerification().averageScore() +
                        request.dashboardAndIndicatorsVerification().averageScore() +
                        request.interiorVerification().averageScore();

        if (request.isApproved() != null && request.isApproved() == Boolean.TRUE) {
            technicalVerification.setStatus(VerificationStatus.APPROVED);
            post.setIsVerified(Boolean.TRUE);
        } else {
            if (totalScore >= 60.0) {
                technicalVerification.setStatus(VerificationStatus.APPROVED);
                technicalVerification.setIsApproved(Boolean.TRUE);
                post.setIsVerified(Boolean.TRUE);
            } else {
                technicalVerification.setStatus(VerificationStatus.REJECTED);
                technicalVerification.setIsApproved(Boolean.FALSE);
                post.setIsVerified(Boolean.FALSE);
            }
        }

        technicalVerificationRepository.save(technicalVerification);

        /*String message = "";
        if (technicalVerification.getStatus() == VerificationStatus.APPROVED) {
            message = """
                    <p>Tu verificación técnica para %s %s ha sido aprobada.</p>
                    """.formatted(post.getMake(), post.getModel());
        } else if (technicalVerification.getStatus() == VerificationStatus.REJECTED) {
            message = """
                    <p>Tu verificación técnica para %s %s ha sido rechazada.</p>
                    """.formatted(post.getMake(), post.getModel());
        }
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .to(new String[]{post.getUserAccount().getUser().getEmail()})
                .subject("Verificación técnica procesada")
                .message(message)
                .build();
        mailService.sendMail(sendEmailRequest);*/
    }

    @Override
    public TechnicalVerificationPercentsDTO getPercentsByPostId(Long postId) {
        PostEntity post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("No se encontró la publicación con id " + postId));
        TechnicalVerification technicalVerification = post.getTechnicalVerification();
        return TechnicalVerificationPercentsDTO.builder()
                .brakingSystemVerification(technicalVerification.getBrakingSystemVerification().averageScore())
                .chassisVerification(technicalVerification.getChassisVerification().averageScore())
                .dashboardAndIndicatorsVerification(technicalVerification.getDashboardAndIndicatorsVerification().averageScore())
                .interiorVerification(technicalVerification.getInteriorVerification().averageScore())
                .motorVerification(technicalVerification.getMotorVerification().averageScore())
                .paintAndBodyworkVerification(technicalVerification.getPaintAndBodyworkVerification().averageScore())
                .suspensionAndSteeringVerification(technicalVerification.getSuspensionAndSteeringVerification().averageScore())
                .tiresAndWheelsVerification(technicalVerification.getTiresAndWheelsVerification().averageScore())
                .build();
    }

    @Override
    public VerificationStatusResponse getStatusByPostId(Long postId) throws PostNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("No se encontró la publicación con id " + postId));
        TechnicalVerification technicalVerification = post.getTechnicalVerification();
        if (technicalVerification == null) {
            throw new PostNotFoundException("No se encontró verificación técnica para la publicación con id " + postId);
        }
        return VerificationStatusResponse.builder()
                .status(technicalVerification.getStatus())
                .sentToVerificationDate(technicalVerification.getSentToVerificationDate())
                .verificationAcceptedDate(technicalVerification.getVerificationAcceptedDate())
                .verificationAppointmentDate(technicalVerification.getVerificationAppointmentDate())
                .isApproved(technicalVerification.getIsApproved())
                .verificationMadeDate(technicalVerification.getVerificationMadeDate())
                .build();
    }

}
