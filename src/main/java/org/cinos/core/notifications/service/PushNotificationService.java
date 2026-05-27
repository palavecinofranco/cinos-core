package org.cinos.core.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cinos.core.notifications.dto.PushNotificationRequest;
import org.cinos.core.notifications.dto.PushNotificationResponse;
import org.cinos.core.notifications.entity.PushTokenEntity;
import org.cinos.core.notifications.repository.PushTokenRepository;
import org.cinos.core.users.entity.UserEntity;
import org.cinos.core.users.repository.UserRepository;
import org.cinos.core.users.model.Role;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;
    private final FirebaseService firebaseService;

    /**
     * Registra un token de notificación para un usuario
     */
    public void registerToken(UserEntity user, String token, PushTokenEntity.DeviceType deviceType) {
        try {
            // Verificar si el token ya existe
            if (pushTokenRepository.existsByToken(token)) {
                log.info("Token ya registrado: {}", token);
                return;
            }

            PushTokenEntity pushToken = PushTokenEntity.builder()
                    .user(user)
                    .token(token)
                    .deviceType(deviceType)
                    .isActive(true)
                    .build();

            pushTokenRepository.save(pushToken);
            log.info("Token registrado exitosamente para usuario: {}", user.getEmail());

            // Si el usuario es premium y Firebase está disponible, suscribirlo al topic premium
            if (user.getRoles() != null && user.getRoles().contains(Role.PREMIUM) && firebaseService.isFirebaseAvailable()) {
                firebaseService.subscribeToTopic(List.of(token), "premium_users");
            }

        } catch (Exception e) {
            log.error("Error registrando token: {}", e.getMessage());
            throw new RuntimeException("Error registrando token de notificación", e);
        }
    }

    /**
     * Elimina un token de notificación
     */
    public void unregisterToken(String token) {
        try {
            pushTokenRepository.deleteByToken(token);
            log.info("Token eliminado: {}", token);
        } catch (Exception e) {
            log.error("Error eliminando token: {}", e.getMessage());
        }
    }

    /**
     * Envía notificación a usuarios específicos
     */
    public PushNotificationResponse sendNotificationToUsers(PushNotificationRequest request) {
        try {
            // Verificar si Firebase está disponible
            if (!firebaseService.isFirebaseAvailable()) {
                log.warn("Firebase no está disponible. No se pueden enviar notificaciones push.");
                return PushNotificationResponse.builder()
                        .success(false)
                        .message("Firebase no está disponible")
                        .totalTokens(0)
                        .successfulDeliveries(0)
                        .failedDeliveries(0)
                        .build();
            }

            List<String> tokens = new ArrayList<>();

            // Si se especifican tokens directamente
            if (request.getTokens() != null && !request.getTokens().isEmpty()) {
                tokens.addAll(request.getTokens());
            }

            // Si se especifican user IDs
            if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
                List<PushTokenEntity> userTokens = pushTokenRepository.findByUserIdsAndActive(request.getUserIds());
                tokens.addAll(userTokens.stream()
                        .map(PushTokenEntity::getToken)
                        .collect(Collectors.toList()));
            }

            // Si se solicita enviar solo a usuarios premium
            if (request.isSendToPremiumOnly()) {
                List<PushTokenEntity> premiumTokens = pushTokenRepository.findActiveTokensForPremiumUsers(Role.PREMIUM);
                tokens = premiumTokens.stream()
                        .map(PushTokenEntity::getToken)
                        .collect(Collectors.toList());
            }

            if (tokens.isEmpty()) {
                return PushNotificationResponse.builder()
                        .success(false)
                        .message("No se encontraron tokens válidos")
                        .totalTokens(0)
                        .successfulDeliveries(0)
                        .failedDeliveries(0)
                        .build();
            }

            // Enviar notificación
            var batchResponse = firebaseService.sendNotificationToTokens(
                    tokens,
                    request.getTitle(),
                    request.getBody(),
                    request.getData()
            );

            // Si Firebase no está disponible, batchResponse será null
            if (batchResponse == null) {
                return PushNotificationResponse.builder()
                        .success(false)
                        .message("Firebase no está disponible")
                        .totalTokens(tokens.size())
                        .successfulDeliveries(0)
                        .failedDeliveries(tokens.size())
                        .build();
            }

            // Actualizar lastUsedAt para tokens exitosos
            updateLastUsedAt(tokens);

            return PushNotificationResponse.builder()
                    .success(true)
                    .message("Notificación enviada exitosamente")
                    .totalTokens(tokens.size())
                    .successfulDeliveries((int) batchResponse.getSuccessCount())
                    .failedDeliveries((int) batchResponse.getFailureCount())
                    .successfulTokens(tokens) // En una implementación real, deberías filtrar los exitosos
                    .build();

        } catch (Exception e) {
            log.error("Error enviando notificación: {}", e.getMessage());
            return PushNotificationResponse.builder()
                    .success(false)
                    .message("Error enviando notificación: " + e.getMessage())
                    .totalTokens(0)
                    .successfulDeliveries(0)
                    .failedDeliveries(0)
                    .build();
        }
    }

    /**
     * Envía notificación a todos los usuarios premium
     */
    public PushNotificationResponse sendNotificationToPremiumUsers(String title, String body, Map<String, String> data) {
        PushNotificationRequest request = PushNotificationRequest.builder()
                .title(title)
                .body(body)
                .data(data)
                .sendToPremiumOnly(true)
                .build();

        return sendNotificationToUsers(request);
    }

    /**
     * Envía notificación a un topic específico
     */
    public void sendNotificationToTopic(String topic, String title, String body, Map<String, String> data) {
        firebaseService.sendNotificationToTopic(topic, title, body, data);
    }

    /**
     * Actualiza el lastUsedAt de los tokens
     */
    private void updateLastUsedAt(List<String> tokens) {
        tokens.forEach(token -> {
            pushTokenRepository.findByToken(token).ifPresent(pushToken -> {
                pushToken.setLastUsedAt(LocalDateTime.now());
                pushTokenRepository.save(pushToken);
            });
        });
    }

    /**
     * Limpia tokens inactivos (más de 30 días sin uso)
     */
    public void cleanupInactiveTokens() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<PushTokenEntity> inactiveTokens = pushTokenRepository.findAll().stream()
                    .filter(token -> token.getLastUsedAt() != null && token.getLastUsedAt().isBefore(thirtyDaysAgo))
                    .collect(Collectors.toList());

            pushTokenRepository.deleteAll(inactiveTokens);
            log.info("Eliminados {} tokens inactivos", inactiveTokens.size());
        } catch (Exception e) {
            log.error("Error limpiando tokens inactivos: {}", e.getMessage());
        }
    }
} 