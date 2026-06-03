package org.cinos.core.technical_verification.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cinos.core.mail.models.SendEmailRequest;
import org.cinos.core.mail.service.MailService;
import org.cinos.core.posts.entity.PostImageEntity;
import org.cinos.core.posts.service.impl.StorageService;
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
import org.cinos.core.users.repository.UserRepository;
import org.cinos.core.utils.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicalVerificationService implements ITechnicalVerificationService {

    private final PostRepository postRepository;
    private final MailService mailService;
    private final TechnicalVerificationRepository technicalVerificationRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final int MAX_IMAGE_ATTACHMENTS = 5;

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

        TechnicalVerification technicalVerification = post.getTechnicalVerification();
        technicalVerification.setStatus(VerificationStatus.SENT);
        technicalVerification.setSentToVerificationDate(LocalDateTime.now());
        technicalVerificationRepository.save(technicalVerification);
        log.info("Verificación técnica solicitada para la publicación con id {} por el usuario {}. Estado: {}. Créditos restantes: {}",
                orderVerificationRequest.postId(), user.getEmail(), technicalVerification.getStatus(), user.getTechnicalVerificationCredits() - 1);

        Map<String, Object> variables = new HashMap<>();
        variables.put("postId", post.getId());
        variables.put("vehicleMake", post.getMake());
        variables.put("vehicleModel", post.getModel());
        variables.put("vehicleYear", post.getYear());
        variables.put("vehicleKilometers", post.getKilometers());
        variables.put("vehicleTransmission", post.getTransmission());
        variables.put("vehicleFuel", post.getFuel());
        variables.put("vehicleHp", post.getHp());
        variables.put("vehicleMotor", post.getMotor());
        variables.put("userName", user.getName());
        variables.put("userLastname", user.getLastname());
        variables.put("userEmail", user.getEmail());
        variables.put("userPhone", orderVerificationRequest.userPhone());
        variables.put("vehicleAddress", post.getLocation().getAddress());
        variables.put("requestDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        variables.put("postUrl", frontendUrl + "/post/" + post.getId());

        Map<String, byte[]> imageAttachments = downloadPostImages(post);

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .to(new String[]{"fpalavecino@cinos.org"})
                .subject("Verificación técnica solicitada - " + post.getMake() + " " + post.getModel())
                .templateName("verification-order")
                .templateVariables(variables)
                .binaryAttachments(imageAttachments)
                .build();
        mailService.sendMail(emailRequest);

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

        log.info("Verificación técnica aceptada para la publicación con id {}. Fecha de turno: {}. Usuario: {}",
                postId, verificationAppointment, post.getUserAccount().getUser().getEmail());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm 'hs'");
        UserEntity owner = post.getUserAccount().getUser();

        Map<String, Object> userVars = new HashMap<>();
        userVars.put("vehicleMake", post.getMake());
        userVars.put("vehicleModel", post.getModel());
        userVars.put("vehicleYear", post.getYear());
        userVars.put("appointmentDate", verificationAppointment.format(formatter));
        userVars.put("isForTechnician", false);
        userVars.put("postId", post.getId());
        userVars.put("postUrl", frontendUrl + "/post/" + post.getId());

        SendEmailRequest userEmail = SendEmailRequest.builder()
                .to(new String[]{owner.getEmail()})
                .subject("Tu verificación técnica fue aceptada - " + post.getMake() + " " + post.getModel())
                .templateName("verification-accepted")
                .templateVariables(userVars)
                .build();
        mailService.sendMail(userEmail);

        Map<String, Object> techVars = new HashMap<>();
        techVars.put("vehicleMake", post.getMake());
        techVars.put("vehicleModel", post.getModel());
        techVars.put("vehicleYear", post.getYear());
        techVars.put("appointmentDate", verificationAppointment.format(formatter));
        techVars.put("isForTechnician", true);
        techVars.put("postId", post.getId());
        techVars.put("postUrl", frontendUrl + "/post/" + post.getId());
        techVars.put("userName", owner.getName());
        techVars.put("userLastname", owner.getLastname());
        techVars.put("userEmail", owner.getEmail());
        techVars.put("userPhone", owner.getPhone());
        techVars.put("vehicleAddress", post.getLocation().getAddress());

        SendEmailRequest techEmail = SendEmailRequest.builder()
                .to(new String[]{"fpalavecino@cinos.org"})
                .subject("Verificación aceptada - Turno " + verificationAppointment.format(formatter))
                .templateName("verification-accepted")
                .templateVariables(techVars)
                .build();
        mailService.sendMail(techEmail);
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

        log.info("Verificación técnica procesada para la publicación con id {}. Puntaje total: {}. Estado: {}. Aprobada: {}",
                request.postId(), totalScore, technicalVerification.getStatus(), technicalVerification.getIsApproved());

        Map<String, Object> resultVars = new HashMap<>();
        resultVars.put("vehicleMake", post.getMake());
        resultVars.put("vehicleModel", post.getModel());
        resultVars.put("postUrl", frontendUrl + "/post/" + post.getId());
        resultVars.put("isApproved", technicalVerification.getStatus() == VerificationStatus.APPROVED);
        resultVars.put("totalScore", totalScore);
        resultVars.put("motorScore", request.motorVerification().averageScore());
        resultVars.put("chassisScore", request.chassisVerification().averageScore());
        resultVars.put("suspensionScore", request.suspensionAndSteeringVerification().averageScore());
        resultVars.put("brakingScore", request.brakingSystemVerification().averageScore());
        resultVars.put("tiresScore", request.tiresAndWheelsVerification().averageScore());
        resultVars.put("paintScore", request.paintAndBodyworkVerification().averageScore());
        resultVars.put("dashboardScore", request.dashboardAndIndicatorsVerification().averageScore());
        resultVars.put("interiorScore", request.interiorVerification().averageScore());

        SendEmailRequest resultEmail = SendEmailRequest.builder()
                .to(new String[]{post.getUserAccount().getUser().getEmail()})
                .subject("Resultado de verificación técnica - " + post.getMake() + " " + post.getModel())
                .templateName("verification-result")
                .templateVariables(resultVars)
                .build();
        mailService.sendMail(resultEmail);
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

    private Map<String, byte[]> downloadPostImages(PostEntity post) {
        Map<String, byte[]> result = new LinkedHashMap<>();
        List<PostImageEntity> images = post.getImages();
        if (images == null || images.isEmpty()) return result;

        int limit = Math.min(images.size(), MAX_IMAGE_ATTACHMENTS);
        for (int i = 0; i < limit; i++) {
            String url = images.get(i).getUrl();
            if (url == null) continue;
            try {
                String blobName = extractBlobName(url);
                byte[] bytes = storageService.downloadFile(blobName);
                String ext = blobName.contains(".") ? blobName.substring(blobName.lastIndexOf('.')) : ".jpg";
                result.put("imagen_" + (i + 1) + ext, bytes);
            } catch (Exception e) {
                log.warn("No se pudo descargar imagen del vehículo: {}", url);
            }
        }
        return result;
    }

    private String extractBlobName(String mediaLink) {
        // GCS media link: https://storage.googleapis.com/download/storage/v1/b/BUCKET/o/BLOB_NAME?alt=media&...
        int oIndex = mediaLink.indexOf("/o/");
        if (oIndex < 0) return mediaLink;
        String encoded = mediaLink.substring(oIndex + 3);
        int qIndex = encoded.indexOf('?');
        if (qIndex >= 0) encoded = encoded.substring(0, qIndex);
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }

}
