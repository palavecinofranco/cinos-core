package org.cinos.core.posts.service.impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${google.cloud.storage.bucket-name}")
    private String bucketName;
    private final Storage storage;

    // Configuraciones para el procesamiento de imágenes
    private static final int MAX_WIDTH = 3840; // 4K support
    private static final int MAX_HEIGHT = 2160; // 4K support
    private static final int THUMBNAIL_WIDTH = 400;
    private static final int THUMBNAIL_HEIGHT = 300;
    private static final float JPEG_QUALITY = 0.85f;
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    public StorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            // Validar tamaño del archivo
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("El archivo " + file.getOriginalFilename() + " excede el tamaño máximo de " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
            }
            
            // Procesar y optimizar la imagen
            byte[] processedImage = processAndOptimizeImage(file);
            
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Bucket bucket = storage.get(bucketName);
            Blob blob = bucket.create(fileName, processedImage);
            fileUrls.add(blob.getMediaLink());
        }
        return fileUrls;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // Validar tamaño del archivo
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo " + file.getOriginalFilename() + " excede el tamaño máximo de " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
        
        // Procesar y optimizar la imagen
        byte[] processedImage = processAndOptimizeImage(file);
        
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Bucket bucket = storage.get(bucketName);
        Blob blob = bucket.create(fileName, processedImage);
        return blob.getMediaLink();
    }

    /**
     * Procesa y optimiza una imagen antes de subirla
     */
    private byte[] processAndOptimizeImage(MultipartFile file) throws IOException {
        // Leer la imagen original
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new IllegalArgumentException("No se pudo leer la imagen: " + file.getOriginalFilename());
        }

        // Redimensionar si es necesario
        BufferedImage resizedImage = resizeImageIfNeeded(originalImage);
        
        // Convertir a byte array optimizado
        return convertToOptimizedByteArray(resizedImage, getFileExtension(file.getOriginalFilename()));
    }

    /**
     * Redimensiona la imagen si excede las dimensiones máximas
     */
    private BufferedImage resizeImageIfNeeded(BufferedImage originalImage) throws IOException {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Si la imagen es más pequeña que el máximo, no redimensionar
        if (originalWidth <= MAX_WIDTH && originalHeight <= MAX_HEIGHT) {
            return originalImage;
        }
        
        // Calcular nuevas dimensiones manteniendo la proporción
        double scale = Math.min(
            (double) MAX_WIDTH / originalWidth,
            (double) MAX_HEIGHT / originalHeight
        );
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        return Thumbnails.of(originalImage)
                .size(newWidth, newHeight)
                .keepAspectRatio(true)
                .asBufferedImage();
    }

    /**
     * Convierte la imagen a byte array optimizado
     */
    private byte[] convertToOptimizedByteArray(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        if ("png".equalsIgnoreCase(format)) {
            // Para PNG, mantener calidad pero optimizar
            ImageIO.write(image, "PNG", outputStream);
        } else {
            // Para JPEG y otros formatos, aplicar compresión
            Thumbnails.of(image)
                    .scale(1.0)
                    .outputQuality(JPEG_QUALITY)
                    .outputFormat("JPEG")
                    .toOutputStream(outputStream);
        }
        
        return outputStream.toByteArray();
    }

    /**
     * Obtiene la extensión del archivo
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Crea una miniatura de la imagen
     */
    public byte[] createThumbnail(byte[] originalImageBytes) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageBytes));
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Thumbnails.of(originalImage)
                .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                .keepAspectRatio(true)
                .outputQuality(JPEG_QUALITY)
                .outputFormat("JPEG")
                .toOutputStream(outputStream);
        
        return outputStream.toByteArray();
    }

    /**
     * Descarga un archivo del bucket
     */
    public byte[] downloadFile(String fileName) throws IOException {
        Blob blob = storage.get(bucketName, fileName);
        return blob.getContent();
    }

    /**
     * Obtiene la URL pública de una imagen
     */
    public String getImageUrl(String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        return blob != null ? blob.getMediaLink() : null;
    }

    /**
     * Elimina un archivo del bucket
     */
    public boolean deleteFile(String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        if (blob != null) {
            return blob.delete();
        }
        return false;
    }
}
