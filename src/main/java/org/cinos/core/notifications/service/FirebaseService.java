package org.cinos.core.notifications.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FirebaseService {

    @Autowired(required = false)
    private FirebaseMessaging firebaseMessaging;

    @PostConstruct
    public void initialize() {
        if (firebaseMessaging != null) {
            log.info("Firebase Messaging inicializado correctamente");
        } else {
            log.warn("FirebaseMessaging no está disponible. Las notificaciones push no funcionarán.");
        }
    }

    public BatchResponse sendNotificationToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            log.warn("Firebase no está disponible. No se pueden enviar notificaciones push.");
            return null;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : Map.of())
                    .addAllTokens(tokens)
                    .build();

            BatchResponse response = firebaseMessaging.sendMulticast(message);
            log.info("Notificación enviada a {} tokens, {} exitosos, {} fallidos", 
                    tokens.size(), response.getSuccessCount(), response.getFailureCount());

            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Error enviando notificación: {}", e.getMessage());
            throw new RuntimeException("Error enviando notificación push", e);
        }
    }

    public String sendNotificationToTopic(String topic, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            log.warn("Firebase no está disponible. No se pueden enviar notificaciones push al topic.");
            return null;
        }

        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : Map.of())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Notificación enviada al topic {}: {}", topic, response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Error enviando notificación al topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Error enviando notificación push al topic", e);
        }
    }

    public void subscribeToTopic(List<String> tokens, String topic) {
        if (firebaseMessaging == null) {
            log.warn("Firebase no está disponible. No se pueden suscribir al topic.");
            return;
        }

        try {
            TopicManagementResponse response = firebaseMessaging.subscribeToTopic(tokens, topic);
            log.info("Suscritos al topic {}: {} exitosos, {} fallidos", 
                    topic, response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("Error suscribiendo al topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Error suscribiendo al topic", e);
        }
    }

    public void unsubscribeFromTopic(List<String> tokens, String topic) {
        if (firebaseMessaging == null) {
            log.warn("Firebase no está disponible. No se pueden desuscribir del topic.");
            return;
        }

        try {
            TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(tokens, topic);
            log.info("Desuscritos del topic {}: {} exitosos, {} fallidos", 
                    topic, response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("Error desuscribiendo del topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Error desuscribiendo del topic", e);
        }
    }

    public boolean isFirebaseAvailable() {
        return firebaseMessaging != null;
    }
} 