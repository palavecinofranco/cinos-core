package org.cinos.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

@Configuration
public class GoogleCloudStorageConfig {

    @Value("${GCP_CREDENTIALS_BASE64}")
    private String gcpCredentialsBase64;

    @Bean
    public Storage storage() throws IOException {
        // Decodificar y escribir a un archivo temporal
        byte[] decoded = Base64.getDecoder().decode(gcpCredentialsBase64);
        Path tempFile = Files.createTempFile("gcp", ".json");
        Files.write(tempFile, decoded);

        // Construir el Storage client con el archivo temporal
        GoogleCredentials credentials = GoogleCredentials.fromStream(Files.newInputStream(tempFile));
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }
}

