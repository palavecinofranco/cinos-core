package org.cinos.core.posts.service.impl;

import org.cinos.core.mail.models.SendEmailRequest;
import org.cinos.core.mail.service.MailService;
import org.cinos.core.posts.controller.request.PostCreateRequest;
import org.cinos.core.posts.dto.PostDTO;
import org.cinos.core.posts.dto.PostFeedDTO;
import org.cinos.core.posts.dto.PostFilterDTO;
import org.cinos.core.posts.dto.PostProfileDTO;
import org.cinos.core.posts.dto.mapper.PostMapper;
import org.cinos.core.posts.entity.*;
import org.cinos.core.posts.models.DocumentationStatus;
import org.cinos.core.posts.models.VerificationStatus;
import org.cinos.core.posts.repository.PostImageRepository;
import org.cinos.core.posts.repository.PostLocationRepository;
import org.cinos.core.posts.repository.PostRepository;
import org.cinos.core.technical_verification.repository.TechnicalVerificationRepository;
import org.cinos.core.posts.repository.specs.PostSpecifications;
import org.cinos.core.posts.service.IMakeService;
import org.cinos.core.posts.service.IModelService;
import org.cinos.core.posts.service.IPostService;
import org.cinos.core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.core.follows.service.IFollowService;
import org.cinos.core.technical_verification.entity.TechnicalVerification;
import org.cinos.core.users.dto.UserDTO;
import org.cinos.core.users.service.impl.AccountService;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cinos.core.users.service.impl.UserService;
import org.cinos.core.notifications.service.AutomaticNotificationService;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {

    private final PostRepository postRepository;
    private final IFollowService followService;
    private final String POST_NOT_FOUND = "La publicacion no se encontró";
    private final StorageService storageService;
    private final PostImageRepository postImageRepository;
    private final PostLocationRepository postLocationRepository;
    private final AccountService accountService;
    private final PostMapper postMapper;
    private final IMakeService makeService;
    private final IModelService modelService;
    private final MailService mailService;
    private final TechnicalVerificationRepository technicalVerificationRepository;
    private final UserService userService;
    private final AutomaticNotificationService automaticNotificationService;
    private final ImageProcessingService imageProcessingService;

    @Override
    public List<PostDTO> getPostPageable(Integer page, Integer size) {
        List<PostEntity> entityList = postRepository.findAll(PageRequest.of(page, size)).toList();
        return entityList.stream().map(postMapper::toDTO).toList();
    }

    @Override
    public Page<PostFeedDTO> getFeedPosts(
            Long userId,
            Pageable pageable,
            Double userLatitude,
            Double userLongitude
    ) throws UserNotFoundException {
        List<UserDTO> followings = followService.getFollowings(userId);
        List<Long> followingsIds = followings.stream().map(UserDTO::id).toList();

        // Obtener preferencias del usuario
        var user = userService.getByIdEntity(userId);
        String preferredBrand = user.getPreferredBrand();
        Boolean wantsUsedCars = user.getWantsUsedCars();
        Boolean wantsNewCars = user.getWantsNewCars();
        Boolean useLocationForRecommendations = user.getUseLocationForRecommendations();

        // Especificación con timeFactor, comentarios, ubicación, relación y preferencias
        Specification<PostEntity> spec = PostSpecifications.postFeedSpec(
                followingsIds,
                userLatitude,
                userLongitude,
                userId,
                preferredBrand,
                wantsUsedCars,
                wantsNewCars,
                useLocationForRecommendations
        );

        // Obtener publicaciones paginadas y ordenadas por relevancia
        Page<PostEntity> postEntityPage = postRepository.findAll(spec, pageable);

        // Mapear a DTO
        return postEntityPage.map((e) -> {
            String userFullName = e.getUserAccount().getUser().getName() + " " + e.getUserAccount().getUser().getLastname();
            return PostFeedDTO.builder()
                    .id(e.getId())
                    .model(e.getModel())
                    .price(e.getPrice())
                    .year(e.getYear())
                    .make(e.getMake())
                    .isUsed(e.getIsUsed())
                    .userFullName(userFullName)
                    .publicationDate(e.getPublicationDate().atZone(ZoneId.systemDefault()))
                    .imagesUrls(e.getImages().stream().map(PostImageEntity::getUrl).toList())
                    .currencySymbol(e.getCurrencySymbol())
                    .location(postMapper.toLocationDTO(e.getLocation()))
                    .kilometers(e.getKilometers())
                    .userId(e.getUserAccount().getId())
                    .isVerified(e.getIsVerified())
                    .build();
        });
    }

    @Override
    public Page<PostDTO> getFollowingsPosts(Long userId, Pageable pageable) throws UserNotFoundException {
        List<UserDTO> followings = followService.getFollowings(userId);
        List<Long> followingsIds = followings.stream().map(UserDTO::id).toList();
        return postRepository.findAllByUserAccount_IdInOrderByPublicationDateDesc(followingsIds, pageable).map(postMapper::toDTO);
    }

    @Override
    public PostDTO getById(Long id) throws PostNotFoundException {
        PostEntity postEntity = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(POST_NOT_FOUND));
        PostDTO dto = postMapper.toDTO(postRepository.findById(id).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND)));
        return postMapper.toDTO(postRepository.findById(id).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND)));
    }

    @Override
    public List<PostDTO> getByUserId(Long userId) {
        return postRepository.findByUserAccount_Id(userId).stream().map(postMapper::toDTO).toList();
    }

    @Override
    public PostDTO createPost(PostCreateRequest request, List<MultipartFile> images) throws IOException, UserNotFoundException {
        makeService.findByName(request.make()).orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        modelService.findByName(request.model()).orElseThrow(() -> new RuntimeException("Modelo no encontrado"));

        PostEntity postEntity = PostEntity.builder()
                .model(request.model())
                .make(request.make())
                .kilometers(request.kilometers())
                .fuel(request.fuel())
                .transmission(request.transmission())
                .year(request.year())
                .isUsed(request.isUsed())
                .price(request.price())
                .userAccount(accountService.getAccountEntityById(request.userId()))
                .publicationDate(LocalDateTime.now())
                .active(Boolean.TRUE)
                .currencySymbol(request.currencySymbol())
                .documentationStatus(DocumentationStatus.NOT_PROVIDED)
                .hp(request.hp())
                .motor(request.motor())
                .traccion(request.traccion())
                .build();

        TechnicalVerification technicalVerification = TechnicalVerification.builder()
                .post(postEntity)
                .status(VerificationStatus.NOT_STARTED)
                .build();
        
        // Procesar imágenes con múltiples resoluciones
        List<PostImageEntity> imagesEntity = new ArrayList<>();
        for (MultipartFile image : images) {
            try {
                // Obtener información de la imagen original
                ImageProcessingService.ImageInfo imageInfo = imageProcessingService.getImageInfo(image);
                
                // Procesar imagen con múltiples resoluciones
                Map<String, String> imageResolutions = imageProcessingService.processImageWithMultipleResolutions(image);
                
                // Guardar la URL original como principal
                String originalUrl = imageResolutions.get("original");
                PostImageEntity imageEntity = PostImageEntity.builder()
                        .url(originalUrl)
                        .post(postEntity)
                        .build();
                imagesEntity.add(imageEntity);
                
                // Guardar información de las dimensiones originales en metadata si es necesario
                // Esto permitirá restaurar la imagen a su tamaño original más tarde
                
            } catch (Exception e) {
                throw new RuntimeException("Error procesando imagen: " + image.getOriginalFilename() + " - " + e.getMessage());
            }
        }

        PostLocationEntity location = PostLocationEntity.builder()
                .address(request.location().address())
                .lat(request.location().lat())
                .lng(request.location().lng())
                .post(postEntity)
                .build();

        postEntity.setImages(imagesEntity);
        postEntity.setLocation(location);
        postRepository.save(postEntity);
        technicalVerificationRepository.save(technicalVerification);
        postImageRepository.saveAll(imagesEntity);
        postLocationRepository.save(location);
        
        // Enviar notificación a usuarios premium sobre el nuevo post
        try {
            automaticNotificationService.notifyNewPost(postEntity);
        } catch (Exception e) {
            // Log del error pero no fallar la creación del post
            System.err.println("Error enviando notificación de nuevo post: " + e.getMessage());
        }
        
        return postMapper.toDTO(postEntity);
    }

    @Override
    public List<PostProfileDTO> getPostsProfile(Long userId) throws UserNotFoundException {
        List<PostEntity> posts = postRepository.findAllByUserAccount_IdAndActiveTrue(userId);
        return posts.stream().map(e -> PostProfileDTO.builder()
                .id(e.getId())
                .firstImage(e.getImages().get(0).getUrl())
                .build()).toList();
    }

    @Override
    public PostEntity getPostEntityById(Long id) throws PostNotFoundException {
        return postRepository.findById(id).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
    }

    @Override
    public List<PostProfileDTO> getSavedPostsProfile(final Long userId) {
        List<PostEntity> posts = postRepository.findByUsersSaved_Id(userId);
        return posts.stream().map(e -> PostProfileDTO.builder()
                .id(e.getId())
                .firstImage(e.getImages().get(0).getUrl())
                .build()).toList();

    }

    @Override
    public void saveUserPost(final Long userId, final Long postId) throws PostNotFoundException, UserNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
        post.getUsersSaved().add(accountService.getAccountEntityById(userId));
        postRepository.save(post);
    }

    @Override
    public Boolean userSavedPost(Long userId, Long postId) throws PostNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
        return post.getUsersSaved().stream().anyMatch(e->e.getId().equals(userId));
    }

    @Override
    public void userUnsavePost(Long userId, Long postId) throws PostNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
        post.getUsersSaved().removeIf(e->e.getId().equals(userId));
        postRepository.save(post);
    }

    @Override
    public void deactivatePost(Long postId) throws PostNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
        post.setActive(Boolean.FALSE);
        postRepository.save(post);
    }

    @Override
    public void uploadDocumentation(Long postId, List<MultipartFile> files) throws PostNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
        String message = """
                Se ha subido la documentación para la publicación de %s %s del año %s, %s km
                """.formatted(post.getMake(), post.getModel(), post.getYear(), post.getKilometers());
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .to(new String[]{"fpalavecino@cinos.org"})
                .subject("Documentación de publicación:" + post.getId())
                .message(message)
                .attachments(files)
                .build();
        try {
            mailService.sendMail(sendEmailRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo", e);
        }
    }

    @Override
    public Page<PostDTO> getPostsFilter(PostFilterDTO postFilterDTO) {
        // Debug: Log del filtro recibido
        System.out.println("=== FILTRO RECIBIDO ===");
        System.out.println("Make: " + postFilterDTO.make());
        System.out.println("Model: " + postFilterDTO.model());
        System.out.println("Fuel: " + postFilterDTO.fuelType());
        System.out.println("Transmission: " + postFilterDTO.transmission());
        System.out.println("Min Year: " + postFilterDTO.minYear());
        System.out.println("Max Year: " + postFilterDTO.maxYear());
        System.out.println("Min Price: " + postFilterDTO.minPrice());
        System.out.println("Max Price: " + postFilterDTO.maxPrice());
        System.out.println("Min Mileage: " + postFilterDTO.minMileage());
        System.out.println("Max Mileage: " + postFilterDTO.maxMileage());
        System.out.println("Is Used: " + postFilterDTO.isUsed());
        System.out.println("Search: " + postFilterDTO.search());
        System.out.println("Page: " + postFilterDTO.page());
        System.out.println("Size: " + postFilterDTO.size());
        System.out.println("=======================");
        
        Page<PostEntity> postPage = postRepository.findAll(PostSpecifications.postFilterSpec(postFilterDTO), PageRequest.of(postFilterDTO.page(), postFilterDTO.size()));
        
        // Debug: Log de resultados
        System.out.println("=== RESULTADOS ===");
        System.out.println("Total elementos: " + postPage.getTotalElements());
        System.out.println("Total páginas: " + postPage.getTotalPages());
        System.out.println("Elementos en esta página: " + postPage.getContent().size());
        System.out.println("==================");
        
        return postPage.map(postMapper::toDTO);
    }

    @Override
    public List<PostDTO> searchPosts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        
        List<PostEntity> posts = postRepository.searchPosts(query.trim());
        return posts.stream()
                .map(postMapper::toDTO)
                .toList();
    }

}
