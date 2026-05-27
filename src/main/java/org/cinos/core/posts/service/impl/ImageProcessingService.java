package org.cinos.core.posts.service.impl;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    // Configuraciones para diferentes resoluciones
    private static final int ORIGINAL_MAX_WIDTH = 3840; // 4K support
    private static final int ORIGINAL_MAX_HEIGHT = 2160; // 4K support
    private static final int MEDIUM_WIDTH = 1200;
    private static final int MEDIUM_HEIGHT = 900;
    private static final int THUMBNAIL_WIDTH = 400;
    private static final int THUMBNAIL_HEIGHT = 300;
    private static final int SMALL_WIDTH = 200;
    private static final int SMALL_HEIGHT = 150;
    
    // Calidades optimizadas para diferentes tamaños
    private static final float HIGH_QUALITY = 0.95f;
    private static final float MEDIUM_QUALITY = 0.85f;
    private static final float LOW_QUALITY = 0.75f;
    
    // Tamaño máximo de archivo (ahora mucho más alto)
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    private final StorageService storageService;

    /**
     * Procesa una imagen y crea múltiples versiones con diferentes resoluciones
     * Ahora maneja imágenes de cualquier tamaño
     */
    public Map<String, String> processImageWithMultipleResolutions(MultipartFile file) throws IOException {
        // Validar el archivo
        validateImageFile(file);
        
        // Leer la imagen original
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("No se pudo leer la imagen: " + file.getOriginalFilename());
        }

        Map<String, String> imageUrls = new HashMap<>();
        String baseFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        
        // Procesar imagen original optimizada (mantiene alta calidad pero comprime)
        byte[] originalBytes = processImage(originalImage, ORIGINAL_MAX_WIDTH, ORIGINAL_MAX_HEIGHT, HIGH_QUALITY, "original");
        String originalUrl = uploadImageToStorage(originalBytes, baseFileName + "_original.jpg");
        imageUrls.put("original", originalUrl);
        
        // Procesar imagen mediana
        byte[] mediumBytes = processImage(originalImage, MEDIUM_WIDTH, MEDIUM_HEIGHT, MEDIUM_QUALITY, "medium");
        String mediumUrl = uploadImageToStorage(mediumBytes, baseFileName + "_medium.jpg");
        imageUrls.put("medium", mediumUrl);
        
        // Procesar miniatura
        byte[] thumbnailBytes = processImage(originalImage, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, MEDIUM_QUALITY, "thumbnail");
        String thumbnailUrl = uploadImageToStorage(thumbnailBytes, baseFileName + "_thumbnail.jpg");
        imageUrls.put("thumbnail", thumbnailUrl);
        
        // Procesar imagen pequeña
        byte[] smallBytes = processImage(originalImage, SMALL_WIDTH, SMALL_HEIGHT, LOW_QUALITY, "small");
        String smallUrl = uploadImageToStorage(smallBytes, baseFileName + "_small.jpg");
        imageUrls.put("small", smallUrl);
        
        return imageUrls;
    }

    /**
     * Procesa una imagen con configuración específica
     * Mejorado para manejar imágenes grandes
     */
    private byte[] processImage(BufferedImage originalImage, int maxWidth, int maxHeight, float quality, String size) throws IOException {
        // Redimensionar si es necesario
        BufferedImage resizedImage = resizeImageIfNeeded(originalImage, maxWidth, maxHeight);
        
        // Aplicar optimización con manejo de memoria mejorado
        return optimizeImage(resizedImage, quality);
    }

    /**
     * Redimensiona la imagen si excede las dimensiones máximas
     * Mejorado para manejar imágenes muy grandes
     */
    private BufferedImage resizeImageIfNeeded(BufferedImage originalImage, int maxWidth, int maxHeight) throws IOException {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Si la imagen es más pequeña que el máximo, no redimensionar
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return originalImage;
        }
        
        // Calcular nuevas dimensiones manteniendo la proporción
        double scale = Math.min(
            (double) maxWidth / originalWidth,
            (double) maxHeight / originalHeight
        );
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        // Usar Thumbnails con configuración optimizada para imágenes grandes
        return Thumbnails.of(originalImage)
                .size(newWidth, newHeight)
                .keepAspectRatio(true)
                .useOriginalFormat()
                .asBufferedImage();
    }

    /**
     * Optimiza la imagen con la calidad especificada
     * Mejorado para manejar archivos grandes
     */
    private byte[] optimizeImage(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // Usar configuración optimizada para compresión
        Thumbnails.of(image)
                .scale(1.0)
                .outputQuality(quality)
                .outputFormat("JPEG")
                .toOutputStream(outputStream);
        
        return outputStream.toByteArray();
    }

    /**
     * Sube una imagen al almacenamiento
     */
    private String uploadImageToStorage(byte[] imageBytes, String fileName) throws IOException {
        // Crear un MultipartFile temporal para usar el StorageService existente
        MultipartFile tempFile = new MultipartFile() {
            @Override
            public String getName() {
                return fileName;
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return "image/jpeg";
            }

            @Override
            public boolean isEmpty() {
                return imageBytes.length == 0;
            }

            @Override
            public long getSize() {
                return imageBytes.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return imageBytes;
            }

            @Override
            public java.io.InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(imageBytes);
            }

            @Override
            public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
                // No implementado para este caso
            }
        };
        
        return storageService.uploadFile(tempFile);
    }

    /**
     * Valida que el archivo sea una imagen válida
     * Ahora permite archivos mucho más grandes
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen válida");
        }
        
        // Validar tamaño máximo (ahora 100MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo de " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
    }

    /**
     * Crea una miniatura de una imagen existente
     */
    public byte[] createThumbnailFromUrl(String imageUrl) throws IOException {
        // Descargar la imagen desde la URL
        byte[] imageBytes = downloadImageFromUrl(imageUrl);
        
        // Crear miniatura
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new IllegalArgumentException("No se pudo leer la imagen desde la URL");
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Thumbnails.of(originalImage)
                .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                .keepAspectRatio(true)
                .outputQuality(MEDIUM_QUALITY)
                .outputFormat("JPEG")
                .toOutputStream(outputStream);
        
        return outputStream.toByteArray();
    }

    /**
     * Descarga una imagen desde una URL
     */
    private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        java.net.URL url = new java.net.URL(imageUrl);
        try (java.io.InputStream inputStream = url.openStream()) {
            return inputStream.readAllBytes();
        }
    }

    /**
     * Obtiene información de una imagen
     */
    public ImageInfo getImageInfo(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("No se pudo leer la imagen");
        }
        
        return ImageInfo.builder()
                .width(image.getWidth())
                .height(image.getHeight())
                .size(file.getSize())
                .format(getImageFormat(file.getOriginalFilename()))
                .build();
    }

    /**
     * Obtiene el formato de la imagen
     */
    private String getImageFormat(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Restaura una imagen a su tamaño original desde una URL
     * Nueva funcionalidad para restaurar imágenes
     */
    public byte[] restoreImageToOriginalSize(String imageUrl, int originalWidth, int originalHeight) throws IOException {
        // Descargar la imagen desde la URL
        byte[] imageBytes = downloadImageFromUrl(imageUrl);
        
        // Leer la imagen
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IllegalArgumentException("No se pudo leer la imagen desde la URL");
        }
        
        // Redimensionar a las dimensiones originales
        BufferedImage restoredImage = Thumbnails.of(image)
                .size(originalWidth, originalHeight)
                .keepAspectRatio(false) // Forzar las dimensiones exactas
                .asBufferedImage();
        
        // Convertir a byte array con alta calidad
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(restoredImage, "JPEG", outputStream);
        
        return outputStream.toByteArray();
    }

    /**
     * Procesa una imagen grande con manejo de memoria optimizado
     * Nueva funcionalidad para imágenes muy grandes
     */
    public Map<String, String> processLargeImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        
        // Leer la imagen original
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("No se pudo leer la imagen: " + file.getOriginalFilename());
        }

        Map<String, String> imageUrls = new HashMap<>();
        String baseFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        
        // Para imágenes grandes, crear versiones más optimizadas
        // Original con compresión inteligente
        byte[] originalBytes = processLargeImageOptimized(originalImage, HIGH_QUALITY);
        String originalUrl = uploadImageToStorage(originalBytes, baseFileName + "_original.jpg");
        imageUrls.put("original", originalUrl);
        
        // Versión mediana
        byte[] mediumBytes = processImage(originalImage, MEDIUM_WIDTH, MEDIUM_HEIGHT, MEDIUM_QUALITY, "medium");
        String mediumUrl = uploadImageToStorage(mediumBytes, baseFileName + "_medium.jpg");
        imageUrls.put("medium", mediumUrl);
        
        // Miniatura
        byte[] thumbnailBytes = processImage(originalImage, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, MEDIUM_QUALITY, "thumbnail");
        String thumbnailUrl = uploadImageToStorage(thumbnailBytes, baseFileName + "_thumbnail.jpg");
        imageUrls.put("thumbnail", thumbnailUrl);
        
        return imageUrls;
    }

    /**
     * Calcula la relación de compresión entre el tamaño original y procesado
     */
    public String calculateCompressionRatio(long originalSize, long processedSize) {
        if (originalSize <= 0) {
            return "0%";
        }
        
        double ratio = ((double) (originalSize - processedSize) / originalSize) * 100;
        return String.format("%.1f%%", ratio);
    }

    /**
     * Procesa una imagen grande con optimizaciones específicas
     */
    private byte[] processLargeImageOptimized(BufferedImage originalImage, float quality) throws IOException {
        // Para imágenes muy grandes, aplicar compresión más agresiva
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Thumbnails.of(originalImage)
                .scale(1.0)
                .outputQuality(quality)
                .outputFormat("JPEG")
                .toOutputStream(outputStream);
        
        return outputStream.toByteArray();
    }

    /**
     * Clase para información de imagen
     */
    public static class ImageInfo {
        private int width;
        private int height;
        private long size;
        private String format;

        public ImageInfo() {}

        public ImageInfo(int width, int height, long size, String format) {
            this.width = width;
            this.height = height;
            this.size = size;
            this.format = format;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int width;
            private int height;
            private long size;
            private String format;

            public Builder width(int width) {
                this.width = width;
                return this;
            }

            public Builder height(int height) {
                this.height = height;
                return this;
            }

            public Builder size(long size) {
                this.size = size;
                return this;
            }

            public Builder format(String format) {
                this.format = format;
                return this;
            }

            public ImageInfo build() {
                return new ImageInfo(width, height, size, format);
            }
        }

        // Getters
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public long getSize() { return size; }
        public String getFormat() { return format; }
    }
}
