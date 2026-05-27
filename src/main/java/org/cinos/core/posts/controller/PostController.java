package org.cinos.core.posts.controller;

import org.cinos.core.posts.controller.request.PostCreateRequest;
import org.cinos.core.posts.dto.*;
import org.cinos.core.posts.service.ICommentService;
import org.cinos.core.posts.service.IMakeService;
import org.cinos.core.posts.service.IModelService;
import org.cinos.core.posts.service.IPostService;
import org.cinos.core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.net.URL;
import java.io.InputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.cinos.core.posts.service.impl.ImageProcessingService;
import org.cinos.core.posts.dto.ImageProcessingResponse;
import org.cinos.core.posts.dto.ImageInfoResponse;
import org.cinos.core.posts.service.impl.ImageProcessingService.ImageInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Map;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);
    private final IPostService postService;
    private final IMakeService makeService;
    private final IModelService modelService;
    private final ICommentService commentService;
    private final ImageProcessingService imageProcessingService;

    @GetMapping("/pageable")
    public ResponseEntity<List<PostDTO>> getPostPageable(@RequestParam final Integer page, @RequestParam final Integer size) {
        return ResponseEntity.ok(postService.getPostPageable(page, size));
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<PostDTO>> getPostsFilter(@RequestBody final PostFilterDTO postFilterDTO) {
        return ResponseEntity.ok(postService.getPostsFilter(postFilterDTO));
    }

    @GetMapping("/feed/{userId}")
    public ResponseEntity<Page<PostFeedDTO>> getFeedPosts(@PathVariable final Long userId,
                                                          @RequestParam final Integer page,
                                                          @RequestParam final Integer size,
                                                          @RequestParam final Double latitude,
                                                          @RequestParam final Double longitude) {
        ResponseEntity<Page<PostFeedDTO>> result = null;
        try {
            result = ResponseEntity.ok(postService.getFeedPosts(userId, PageRequest.of(page, size), latitude, longitude));
        } catch (UserNotFoundException e) {
            log.info(e.getMessage());
        }
        return result;
    }

    @GetMapping("/followings/{userId}")
    public ResponseEntity<Page<PostDTO>> getFollowingsPosts(@PathVariable final Long userId, @RequestParam final Integer page, @RequestParam final Integer size) throws UserNotFoundException {
        return ResponseEntity.ok(postService.getFollowingsPosts(userId, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable final Long id) throws PostNotFoundException {
        return ResponseEntity.ok(postService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDTO>> getPostsByUserId(@PathVariable final Long userId) {
        return ResponseEntity.ok(postService.getByUserId(userId));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<List<PostProfileDTO>> getPostsProfileByUserId(@PathVariable final Long userId) throws UserNotFoundException {
        return ResponseEntity.ok(postService.getPostsProfile(userId));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/profile/{userId}/saved-posts")
    public ResponseEntity<List<PostProfileDTO>> getSavedPostsProfile(@PathVariable final Long userId) throws UserNotFoundException {
        return ResponseEntity.ok(postService.getSavedPostsProfile(userId));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/create")
    public ResponseEntity<PostDTO> createPost(
            @RequestParam("post") final String request,
            @RequestParam("images") final List<MultipartFile> images) throws IOException, UserNotFoundException {
        ObjectMapper objectMapper = new ObjectMapper();
        PostCreateRequest post = objectMapper.readValue(request, PostCreateRequest.class);
        return ResponseEntity.ok(postService.createPost(post, images));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/account/{userId}/save-post/{postId}")
    public ResponseEntity<Object> saveUserPost(@PathVariable final Long userId, @PathVariable final Long postId) throws UserNotFoundException, PostNotFoundException {
        postService.saveUserPost(userId, postId);
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/account/{userId}/unsave-post/{postId}")
    public ResponseEntity<Object> userUnsavePost(@PathVariable final Long userId, @PathVariable final Long postId) throws PostNotFoundException {
        postService.userUnsavePost(userId, postId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/account/{userId}/saved/{postId}")
    public ResponseEntity<Boolean> userSavedPost(@PathVariable final Long userId, @PathVariable final Long postId) throws UserNotFoundException, PostNotFoundException {
        return ResponseEntity.ok(postService.userSavedPost(userId, postId));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/makes")
    public ResponseEntity<List<MakeDTO>> getMakes(@RequestParam final String q) {
        return ResponseEntity.ok(makeService.findByNameContainingIgnoreCase(q));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/models")
    public ResponseEntity<List<ModelDTO>> getModels(@RequestParam final String make) {
        return ResponseEntity.ok(modelService.findAllByMakeName(make));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<CommentDTO>> getComments(@PathVariable final Long id, @RequestParam final Integer page, @RequestParam final Integer size) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(id, PageRequest.of(page, size)));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}/comments-length")
    public ResponseEntity<Integer> getCommentsLength(@PathVariable final Long id) {
        return ResponseEntity.ok(commentService.getCommentsLength(id));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/comment")
    public ResponseEntity<CommentDTO> sendComment(@RequestBody final CommentDTO commentDTO) throws UserNotFoundException {
        return ResponseEntity.ok(commentService.createComment(commentDTO));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Object> deactivatePost(@PathVariable Long id) throws PostNotFoundException {
        postService.deactivatePost(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/upload-documentation")
    public ResponseEntity<Object> uploadDocumentation(@RequestParam("postId") final Long postId,
                                                      @RequestParam("docs") final List<MultipartFile> docs) throws PostNotFoundException, IOException {
        postService.uploadDocumentation(postId, docs);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/technical-verification-status")
    public ResponseEntity<?> getTechnicalVerificationStatus(@PathVariable Long id) {
        try {
            var post = postService.getPostEntityById(id);
            var technicalVerification = post.getTechnicalVerification();
            if (technicalVerification == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(org.cinos.core.technical_verification.dto.VerificationStatusResponse.builder()
                    .status(technicalVerification.getStatus())
                    .sentToVerificationDate(technicalVerification.getSentToVerificationDate())
                    .verificationAcceptedDate(technicalVerification.getVerificationAcceptedDate())
                    .verificationAppointmentDate(technicalVerification.getVerificationAppointmentDate())
                    .isApproved(technicalVerification.getIsApproved())
                    .verificationMadeDate(technicalVerification.getVerificationMadeDate())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("No se pudo obtener el informe técnico: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/download-image")
    public ResponseEntity<InputStreamResource> downloadImage(@RequestBody DownloadImageRequest request) {
        try {
            // Validar que la URL no esté vacía
            if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Validar que la URL sea válida
            String imageUrl = request.getImageUrl().trim();
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                return ResponseEntity.badRequest().build();
            }

            // Crear URL y obtener el stream de la imagen
            URL url = new URL(imageUrl);
            InputStream inputStream = url.openStream();

            // Determinar el tipo de archivo basado en la URL
            String fileName = "imagen.jpg";
            String contentType = MediaType.IMAGE_JPEG_VALUE;
            
            if (imageUrl.toLowerCase().endsWith(".png")) {
                fileName = "imagen.png";
                contentType = MediaType.IMAGE_PNG_VALUE;
            } else if (imageUrl.toLowerCase().endsWith(".gif")) {
                fileName = "imagen.gif";
                contentType = MediaType.IMAGE_GIF_VALUE;
            } else if (imageUrl.toLowerCase().endsWith(".webp")) {
                fileName = "imagen.webp";
                contentType = "image/webp";
            }

            // Crear headers para la descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.set("Cache-Control", "no-cache");

            // Crear el recurso de entrada
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("Error al descargar la imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/process-image")
    public ResponseEntity<ImageProcessingResponse> processImage(@RequestParam("image") MultipartFile image) {
        try {
            // Validar que el archivo sea una imagen
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }

            // Procesar la imagen con múltiples resoluciones
            Map<String, String> imageResolutions = imageProcessingService.processImageWithMultipleResolutions(image);
            
            // Obtener información de la imagen
            ImageProcessingService.ImageInfo imageInfo = imageProcessingService.getImageInfo(image);
            
            ImageProcessingResponse response = ImageProcessingResponse.builder()
                    .originalUrl(imageResolutions.get("original"))
                    .mediumUrl(imageResolutions.get("medium"))
                    .thumbnailUrl(imageResolutions.get("thumbnail"))
                    .smallUrl(imageResolutions.get("small"))
                    .width(imageInfo.getWidth())
                    .height(imageInfo.getHeight())
                    .size(imageInfo.getSize())
                    .format(imageInfo.getFormat())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error procesando imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/process-large-image")
    public ResponseEntity<ImageProcessingResponse> processLargeImage(@RequestParam("image") MultipartFile image) {
        try {
            // Validar que el archivo sea una imagen
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }

            // Procesar la imagen grande con optimizaciones específicas
            Map<String, String> imageResolutions = imageProcessingService.processLargeImage(image);
            
            // Obtener información de la imagen
            ImageProcessingService.ImageInfo imageInfo = imageProcessingService.getImageInfo(image);
            
            ImageProcessingResponse response = ImageProcessingResponse.builder()
                    .originalUrl(imageResolutions.get("original"))
                    .mediumUrl(imageResolutions.get("medium"))
                    .thumbnailUrl(imageResolutions.get("thumbnail"))
                    .width(imageInfo.getWidth())
                    .height(imageInfo.getHeight())
                    .size(imageInfo.getSize())
                    .format(imageInfo.getFormat())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error procesando imagen grande: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/image-info")
    public ResponseEntity<ImageInfoResponse> getImageInfo(@RequestParam String imageUrl) {
        try {
            // Validar URL
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Descargar y analizar la imagen
            URL url = new URL(imageUrl);
            try (InputStream inputStream = url.openStream()) {
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    return ResponseEntity.badRequest().build();
                }

                ImageInfoResponse response = ImageInfoResponse.builder()
                        .width(image.getWidth())
                        .height(image.getHeight())
                        .url(imageUrl)
                        .build();

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            log.error("Error obteniendo información de imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/restore-image")
    public ResponseEntity<InputStreamResource> restoreImageToOriginalSize(
            @RequestParam String imageUrl,
            @RequestParam int originalWidth,
            @RequestParam int originalHeight) {
        try {
            // Validar parámetros
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            if (originalWidth <= 0 || originalHeight <= 0) {
                return ResponseEntity.badRequest().build();
            }

            // Restaurar imagen a su tamaño original
            byte[] restoredImageBytes = imageProcessingService.restoreImageToOriginalSize(
                    imageUrl, originalWidth, originalHeight);

            // Crear headers para la descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("attachment", "imagen_restaurada.jpg");
            headers.set("Cache-Control", "no-cache");

            // Crear el recurso de entrada
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(restoredImageBytes));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("Error restaurando imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
