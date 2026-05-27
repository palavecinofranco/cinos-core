package org.cinos.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.project.id}")
    private String projectId;

    @Value("${firebase.credentials}")
    private String firebaseCredentialsBase64;

    private FirebaseApp firebaseApp;

    @PostConstruct
    public void initializeFirebase() {
        try {
            // Verificar si Firebase ya está inicializado
            if (FirebaseApp.getApps().isEmpty()) {
                
                if (firebaseCredentialsBase64 != null && !firebaseCredentialsBase64.isEmpty()) {
                    // Usar credenciales desde variable de entorno BASE64
                    byte[] decoded = Base64.getDecoder().decode(firebaseCredentialsBase64);
                    Path tempFile = Files.createTempFile("firebase", ".json");
                    Files.write(tempFile, decoded);
                    
                    GoogleCredentials credentials = GoogleCredentials.fromStream(Files.newInputStream(tempFile));
                    
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .setProjectId(projectId)
                            .build();

                    this.firebaseApp = FirebaseApp.initializeApp(options);
                    log.info("Firebase Admin SDK inicializado correctamente para el proyecto: {}", projectId);
                    log.info("Usando credenciales desde variable de entorno FIREBASE_CREDENTIALS_BASE64");
                    
                    // Limpiar archivo temporal
                    Files.deleteIfExists(tempFile);
                } else {
                    log.warn("FIREBASE_CREDENTIALS_BASE64 no está configurada. Firebase no se inicializará.");
                }
            } else {
                this.firebaseApp = FirebaseApp.getInstance();
                log.info("Firebase Admin SDK ya está inicializado");
            }
        } catch (IOException e) {
            log.error("Error cargando credenciales de Firebase desde FIREBASE_CREDENTIALS_BASE64: {}", e.getMessage());
            log.warn("Firebase no se inicializará. Las notificaciones push no funcionarán.");
        } catch (Exception e) {
            log.error("Error inicializando Firebase Admin SDK: {}", e.getMessage());
            log.warn("Firebase no se inicializará. Las notificaciones push no funcionarán.");
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        if (firebaseApp == null) {
            log.warn("Firebase no está inicializado. Creando bean mock para FirebaseMessaging.");
            return null;
        }
        return FirebaseMessaging.getInstance(firebaseApp);
    }
} 