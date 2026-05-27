package org.cinos.core.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cinos.core.notifications.dto.PushNotificationRequest;
import org.cinos.core.notifications.entity.PushTokenEntity;
import org.cinos.core.notifications.repository.PushTokenRepository;
import org.cinos.core.posts.entity.PostEntity;
import org.cinos.core.users.entity.UserEntity;
import org.cinos.core.users.repository.UserRepository;
import org.cinos.core.users.model.Role;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomaticNotificationService {

    private final PushNotificationService pushNotificationService;
    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;

    /**
     * Notifica a usuarios premium sobre un nuevo post
     * Solo envía notificaciones a usuarios que coincidan con sus preferencias
     */
    public void notifyNewPost(PostEntity post) {
        try {
            log.info("Iniciando notificación de nuevo post: {} {} (ID: {})", 
                    post.getMake(), post.getModel(), post.getId());
            
            String title = "🚗 Nuevo vehículo disponible";
            String body = String.format("Se ha publicado un nuevo %s %s", 
                    post.getMake(), post.getModel());

            Map<String, String> data = Map.of(
                    "type", "NEW_POST",
                    "postId", post.getId().toString(),
                    "make", post.getMake(),
                    "model", post.getModel()
            );

            // Obtener tokens de usuarios premium que coincidan con sus preferencias
            List<String> matchingTokens = getPremiumTokensMatchingPreferences(post);

            if (matchingTokens.isEmpty()) {
                log.info("No hay usuarios premium que coincidan con las preferencias para el post: {} {} (ID: {})", 
                        post.getMake(), post.getModel(), post.getId());
                return;
            }

            log.info("Enviando notificación a {} usuarios premium para post {} {} (ID: {})", 
                    matchingTokens.size(), post.getMake(), post.getModel(), post.getId());

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(data)
                    .tokens(matchingTokens)
                    .build();

            var response = pushNotificationService.sendNotificationToUsers(request);
            
            if (response.isSuccess()) {
                log.info("✅ Notificación de nuevo post enviada exitosamente a {} usuarios premium", 
                        response.getSuccessfulDeliveries());
            } else {
                log.error("❌ Error enviando notificación de nuevo post: {}", response.getMessage());
            }
        } catch (Exception e) {
            log.error("❌ Error crítico enviando notificación de nuevo post: {}", e.getMessage(), e);
        }
    }

    /**
     * Obtiene los tokens de usuarios premium que coinciden con las preferencias del post
     */
    private List<String> getPremiumTokensMatchingPreferences(PostEntity post) {
        // Obtener todos los usuarios premium con sus tokens activos
        List<PushTokenEntity> premiumTokens = pushTokenRepository.findActiveTokensForPremiumUsers(Role.PREMIUM);
        
        return premiumTokens.stream()
                .filter(tokenEntity -> {
                    UserEntity user = tokenEntity.getUser();
                    
                    // Verificar que el usuario sea realmente premium
                    if (user.getRoles() == null || !user.getRoles().contains(Role.PREMIUM)) {
                        log.debug("Usuario {} no es premium, saltando notificación", user.getEmail());
                        return false;
                    }
                    
                    // Si el usuario no tiene preferencias configuradas, no recibir notificaciones
                    if (user.getPremiumNotificationBrand() == null && 
                        user.getPremiumNotificationModel() == null && 
                        user.getPremiumNotificationCondition() == null) {
                        log.debug("Usuario premium {} no tiene preferencias configuradas, saltando notificación", user.getEmail());
                        return false;
                    }
                    
                    // Verificar coincidencia de marca
                    if (user.getPremiumNotificationBrand() != null && 
                        !user.getPremiumNotificationBrand().isEmpty() &&
                        !user.getPremiumNotificationBrand().equals(post.getMake())) {
                        log.debug("Usuario {} no coincide con marca preferida: {} vs {}", 
                                user.getEmail(), user.getPremiumNotificationBrand(), post.getMake());
                        return false;
                    }
                    
                    // Verificar coincidencia de modelo
                    if (user.getPremiumNotificationModel() != null && 
                        !user.getPremiumNotificationModel().isEmpty() &&
                        !user.getPremiumNotificationModel().equals(post.getModel())) {
                        log.debug("Usuario {} no coincide con modelo preferido: {} vs {}", 
                                user.getEmail(), user.getPremiumNotificationModel(), post.getModel());
                        return false;
                    }
                    
                    // Verificar coincidencia de condición (usado/nuevo)
                    if (user.getPremiumNotificationCondition() != null && 
                        !user.getPremiumNotificationCondition().isEmpty()) {
                        String postCondition = post.getIsUsed() ? "usado" : "nuevo";
                        if (!user.getPremiumNotificationCondition().equals(postCondition)) {
                            log.debug("Usuario {} no coincide con condición preferida: {} vs {}", 
                                    user.getEmail(), user.getPremiumNotificationCondition(), postCondition);
                            return false;
                        }
                    }
                    
                    log.debug("Usuario premium {} coincide con preferencias para post {} {}", 
                            user.getEmail(), post.getMake(), post.getModel());
                    return true;
                })
                .map(PushTokenEntity::getToken)
                .collect(Collectors.toList());
    }

    /**
     * Notifica cuando se completa una verificación técnica
     */
    public void notifyVerificationCompleted(PostEntity post, String verificationStatus) {
        try {
            String title = "✅ Verificación técnica completada";
            String body = String.format("La verificación técnica del %s %s ha sido %s", 
                    post.getMake(), post.getModel(), verificationStatus);

            Map<String, String> data = Map.of(
                    "type", "VERIFICATION_COMPLETED",
                    "postId", post.getId().toString(),
                    "verificationStatus", verificationStatus
            );

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(data)
                    .sendToPremiumOnly(true)
                    .build();

            pushNotificationService.sendNotificationToUsers(request);
            log.info("Notificación de verificación técnica enviada a usuarios premium");
        } catch (Exception e) {
            log.error("Error enviando notificación de verificación técnica: {}", e.getMessage());
        }
    }

    /**
     * Notifica sobre actualizaciones de suscripción
     */
    public void notifySubscriptionUpdate(UserEntity user, String message) {
        try {
            String title = "💎 Actualización de suscripción";
            String body = message;

            Map<String, String> data = Map.of(
                    "type", "SUBSCRIPTION_UPDATE",
                    "message", message
            );

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(data)
                    .userIds(java.util.List.of(user.getId()))
                    .build();

            pushNotificationService.sendNotificationToUsers(request);
            log.info("Notificación de suscripción enviada al usuario: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error enviando notificación de suscripción: {}", e.getMessage());
        }
    }

    /**
     * Notifica sobre nuevas características premium
     */
    public void notifyPremiumFeature(String featureName, String description) {
        try {
            String title = "🌟 Nueva característica premium";
            String body = String.format("%s: %s", featureName, description);

            Map<String, String> data = Map.of(
                    "type", "PREMIUM_FEATURE",
                    "feature", featureName,
                    "description", description
            );

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(data)
                    .sendToPremiumOnly(true)
                    .build();

            pushNotificationService.sendNotificationToUsers(request);
            log.info("Notificación de nueva característica premium enviada");
        } catch (Exception e) {
            log.error("Error enviando notificación de característica premium: {}", e.getMessage());
        }
    }

    /**
     * Notifica sobre anuncios del sistema
     */
    public void notifySystemAnnouncement(String title, String message) {
        try {
            Map<String, String> data = Map.of(
                    "type", "SYSTEM_ANNOUNCEMENT",
                    "message", message
            );

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(message)
                    .data(data)
                    .sendToPremiumOnly(true)
                    .build();

            pushNotificationService.sendNotificationToUsers(request);
            log.info("Anuncio del sistema enviado a usuarios premium");
        } catch (Exception e) {
            log.error("Error enviando anuncio del sistema: {}", e.getMessage());
        }
    }

    /**
     * Notifica sobre ofertas especiales para usuarios premium
     */
    public void notifyPremiumOffer(String offerTitle, String offerDescription) {
        try {
            String title = "🎁 Oferta especial premium";
            String body = String.format("%s: %s", offerTitle, offerDescription);

            Map<String, String> data = Map.of(
                    "type", "PREMIUM_OFFER",
                    "offerTitle", offerTitle,
                    "offerDescription", offerDescription
            );

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(data)
                    .sendToPremiumOnly(true)
                    .build();

            pushNotificationService.sendNotificationToUsers(request);
            log.info("Oferta premium enviada a usuarios premium");
        } catch (Exception e) {
            log.error("Error enviando oferta premium: {}", e.getMessage());
        }
    }

    /**
     * Método de prueba para verificar el filtrado de preferencias
     * Solo para testing - no usar en producción
     */
    public int getMatchingPremiumUsersCount(PostEntity post) {
        List<String> matchingTokens = getPremiumTokensMatchingPreferences(post);
        return matchingTokens.size();
    }
} 